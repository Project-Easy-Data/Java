import os
import shutil
from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from langchain_community.document_loaders import PyPDFLoader
from langchain_community.vectorstores import Chroma
from langchain_ollama import ChatOllama, OllamaEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter

os.environ["ANONYMIZED_TELEMETRY"] = "False"
os.environ["CHROMA_TELEMETRY"] = "False"

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://ollama:11434")
OLLAMA_EMBED_MODEL = os.getenv("OLLAMA_EMBED_MODEL", "nomic-embed-text")
OLLAMA_CHAT_MODEL = os.getenv("OLLAMA_CHAT_MODEL", "llama3.2:1b")
CHROMA_DIR = os.getenv("CHROMA_DIR", "./db_local")
RAG_DOCUMENTOS = os.getenv("RAG_DOCUMENTOS", "DocumentacaoGrupo2_atualizada.pdf")
RECRIAR_DB = os.getenv("RAG_RECRIAR_DB", "true").lower() == "true"

vector_db = None
llm = None
documentos_carregados = []
total_chunks = 0
todos_chunks = []


def listar_documentos():
    return [Path(nome.strip()) for nome in RAG_DOCUMENTOS.split(",") if nome.strip()]


def carregar_documentos():
    documentos = []

    for caminho in listar_documentos():
        if not caminho.exists():
            print(f"AVISO: documento nao encontrado: {caminho}")
            continue

        print(f"Lendo documento: {caminho}")
        loader = PyPDFLoader(str(caminho))
        paginas = loader.load()

        for pagina in paginas:
            if pagina.page_content and pagina.page_content.strip():
                pagina.metadata["arquivo"] = caminho.name
                pagina.metadata["pagina"] = pagina.metadata.get("page", 0) + 1
                documentos.append(pagina)

        documentos_carregados.append(caminho.name)

    return documentos


def iniciar_rag():
    global vector_db, llm, total_chunks, todos_chunks

    print(">>> Iniciando RAG EasyData com Ollama <<<")

    documentos = carregar_documentos()

    if not documentos:
        print("ERRO: nenhum documento foi carregado.")
        return

    splitter = RecursiveCharacterTextSplitter(
        chunk_size=250,
        chunk_overlap=30,
        separators=["\n\n", "\n", ".", "!", "?", ";", ",", " ", ""],
    )

    chunks = splitter.split_documents(documentos)
    todos_chunks = chunks
    total_chunks = len(chunks)

    if RECRIAR_DB and Path(CHROMA_DIR).exists():
        shutil.rmtree(CHROMA_DIR)

    print(f"Documentos carregados: {documentos_carregados}")
    print(f"Chunks criados: {total_chunks}")
    print("--- Criando banco vetorial ChromaDB ---")

    embeddings = OllamaEmbeddings(
        model=OLLAMA_EMBED_MODEL,
        base_url=OLLAMA_BASE_URL,
    )

    vector_db = Chroma.from_documents(
        documents=chunks,
        embedding=embeddings,
        persist_directory=CHROMA_DIR,
        collection_name="easydata_documentacao",
    )

    llm = ChatOllama(
        model=OLLAMA_CHAT_MODEL,
        base_url=OLLAMA_BASE_URL,
        temperature=0.2,
    )

    print("--- TUDO PRONTO: Sistema de busca ativo! ---")


def expandir_pergunta(pergunta):
    pergunta_lower = pergunta.lower()

    mapa = {
        "easydata projeto": [
            "EasyData Inteligencia Urbana em Saneamento",
            "descricao do projeto",
            "objetivo do projeto",
            "justificativa",
            "mercado imobiliario",
            "saneamento basico",
        ],
        "objetivo": [
            "OBJETIVO",
            "transformar dados urbanos",
            "decisoes estrategicas",
            "mercado imobiliario",
        ],
        "contexto": [
            "CONTEXTO",
            "cenario brasileiro",
            "infraestrutura urbana",
            "saneamento basico",
            "IDH",
            "valorizacao imobiliaria",
        ],
        "problema": [
            "problema",
            "desigualdade regional",
            "infraestrutura precaria",
            "falta de saneamento",
            "decisoes sem dados estruturados",
        ],
        "justificativa": [
            "JUSTIFICATIVA",
            "importancia do projeto",
            "tomada de decisao",
            "dados urbanos",
        ],
        "escopo": [
            "ESCOPO",
            "limites",
            "exclusoes",
            "resultados esperados",
        ],
        "stakeholders": [
            "STAKEHOLDERS",
            "imobiliarias",
            "construtoras",
            "usuarios",
            "mercado imobiliario",
        ],
        "integrantes": [
            "Andre",
            "Claudiana",
            "Gabriel",
            "Giovana",
            "Joao",
            "Rafael",
        ],
        "arquitetura": [
            "ARQUITETURA TECNICA",
            "docker",
            "banco de dados",
            "site institucional",
            "java",
            "rag",
        ],
    }

    extras = []

    if "easydata" in pergunta_lower or "projeto" in pergunta_lower:
        extras.extend(mapa["easydata projeto"])

    for chave, termos in mapa.items():
        if chave in pergunta_lower:
            extras.extend(termos)

    if "integrantes" in pergunta_lower or "grupo" in pergunta_lower or "equipe" in pergunta_lower:
        extras.extend(mapa["integrantes"])

    if "stakeholder" in pergunta_lower or "publico" in pergunta_lower or "público" in pergunta_lower:
        extras.extend(mapa["stakeholders"])

    extras = list(dict.fromkeys(extras))

    return pergunta + "\n" + "\n".join(extras)


def chunk_ruim(texto):
    texto_lower = texto.lower()

    if "sumário" in texto_lower or "sumario" in texto_lower:
        return True

    if texto.count(".") > 20 and len(texto) < 800:
        return True

    if "são paulo" in texto_lower and "2026" in texto_lower and len(texto) < 400:
        return True

    return False


def buscar_por_palavras(pergunta, limite=10):
    consulta = expandir_pergunta(pergunta).lower()
    termos = [
        termo.strip()
        for termo in consulta.replace("\n", " ").split()
        if len(termo.strip()) > 3
    ]

    encontrados = []

    for doc in todos_chunks:
        texto_original = doc.page_content
        texto = texto_original.lower()

        if chunk_ruim(texto_original):
            continue

        pontuacao = sum(1 for termo in termos if termo in texto)

        if pontuacao > 0:
            encontrados.append((pontuacao, doc))

    encontrados.sort(key=lambda item: item[0], reverse=True)

    return [doc for _, doc in encontrados[:limite]]


def montar_contexto(pergunta):
    consulta = expandir_pergunta(pergunta)

    resultados_palavras = buscar_por_palavras(pergunta, limite=10)
    resultados_vetor = vector_db.similarity_search(consulta, k=8)

    resultados = []
    vistos = set()

    for doc in resultados_palavras + resultados_vetor:
        if chunk_ruim(doc.page_content):
            continue

        chave = (
            doc.metadata.get("arquivo", ""),
            doc.metadata.get("pagina", ""),
            doc.page_content[:80],
        )

        if chave not in vistos:
            vistos.add(chave)
            resultados.append(doc)

    contexto = []
    fontes = []

    for doc in resultados:
        metadata = doc.metadata

        fonte = {
            "arquivo": metadata.get("arquivo", metadata.get("source", "")),
            "pagina": metadata.get("pagina", metadata.get("page", "")),
        }

        if fonte not in fontes:
            fontes.append(fonte)

        contexto.append(doc.page_content)

    texto_contexto = "\n\n".join(contexto)

    print("Fontes usadas:", fontes[:3])
    print("Trecho de contexto:", texto_contexto[:1000])

    return texto_contexto[:7000], fontes[:3]


def gerar_resposta(pergunta, contexto):
    if not contexto or len(contexto.strip()) < 50:
        return "Nao encontrei essa informacao na documentacao do projeto."

    prompt = f"""
Voce e o Assistente EasyData, chatbot do site institucional da EasyData.

Responda em portugues do Brasil, de forma clara e objetiva.
Use somente as informacoes do contexto abaixo.

Se a pergunta for "O que e a EasyData?", explique o projeto com base no objetivo,
contexto, descricao, justificativa e resultados esperados encontrados no contexto.

Nao invente dados fora do contexto.
Nao diga que nao encontrou se o contexto tiver qualquer trecho util sobre a EasyData.

Contexto:
{contexto}

Pergunta:
{pergunta}

Resposta:
"""

    resposta = llm.invoke(prompt)
    return resposta.content


try:
    iniciar_rag()
except Exception as e:
    print(f"ERRO CRITICO NA INICIALIZACAO: {str(e)}")


@app.get("/")
def home():
    return {
        "status": "Online" if vector_db and llm else "Sistema nao inicializado",
        "documentos": documentos_carregados,
        "chunks": total_chunks,
        "ollama": OLLAMA_BASE_URL,
        "embed_model": OLLAMA_EMBED_MODEL,
        "chat_model": OLLAMA_CHAT_MODEL,
    }


@app.get("/ask")
async def ask(question: str):
    if vector_db is None or llm is None:
        return {"erro": "O sistema nao foi inicializado."}

    if not question or not question.strip():
        return {"erro": "Envie uma pergunta valida."}

    pergunta = question.strip()
    pergunta_normalizada = pergunta.lower()

    if pergunta_normalizada in ["oi", "ola", "olá", "bom dia", "boa tarde", "boa noite"]:
        return {
            "resposta": "Olá! Sou o Assistente EasyData. Posso responder perguntas sobre o projeto EasyData, saneamento, objetivo, contexto, integrantes e documentação.",
            "fontes": [],
        }

    try:
        print(f"Pergunta recebida: {pergunta}")

        contexto, fontes = montar_contexto(pergunta)
        resposta = gerar_resposta(pergunta, contexto)

        return {
            "resposta": resposta,
            "fontes": fontes,
        }

    except Exception as e:
        print(f"Erro ao processar pergunta: {str(e)}")
        return {"erro": str(e)}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)