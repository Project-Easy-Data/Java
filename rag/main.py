import os
import shutil
import unicodedata
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
RAG_DOCUMENTOS = os.getenv("RAG_DOCUMENTOS", "Documentacao_RAG_EasyData_Resumida.pdf")
RECRIAR_DB = os.getenv("RAG_RECRIAR_DB", "true").lower() == "true"

vector_db = None
llm = None
documentos_carregados = []
total_chunks = 0

INTEGRANTES_EASYDATA = [
    "Andre Luis Costa Santos",
    "Claudiana dos Santos",
    "Gabriel Adryan de Toledo Sacchi",
    "Giovana Querobino Branquinho",
    "Joao Vitor Gomes De Melo",
    "Rafael Prazeres Calderon",
]


def normalizar(texto):
    texto = texto.lower().strip()
    texto = unicodedata.normalize("NFD", texto)
    texto = "".join(c for c in texto if unicodedata.category(c) != "Mn")
    return texto


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
    global vector_db, llm, total_chunks

    print(">>> Iniciando RAG EasyData com Ollama <<<")

    documentos = carregar_documentos()

    if not documentos:
        print("ERRO: nenhum documento foi carregado.")
        return

    splitter = RecursiveCharacterTextSplitter(
        chunk_size=450,
        chunk_overlap=80,
        separators=["\n\n", "\n", ".", "!", "?", ";", ",", " ", ""],
    )

    chunks = splitter.split_documents(documentos)
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
        temperature=0.0,
    )

    print("--- TUDO PRONTO: Sistema de busca ativo! ---")


def pergunta_sobre_integrantes(pergunta):
    p = normalizar(pergunta)
    palavras = [
        "integrantes",
        "membros",
        "equipe",
        "grupo",
        "quem faz parte",
        "quem sao os membros",
        "quem e a equipe",
        "participantes",
    ]
    return any(palavra in p for palavra in palavras)


def resposta_integrantes():
    nomes = "\n".join([f"- {nome}" for nome in INTEGRANTES_EASYDATA])
    return "A equipe do projeto EasyData possui 6 integrantes:\n\n" + nomes


def pergunta_saudacao(pergunta):
    p = normalizar(pergunta)
    return p in ["oi", "ola", "olá", "bom dia", "boa tarde", "boa noite"]


def expandir_pergunta(pergunta):
    p = normalizar(pergunta)
    extras = []

    if "easydata" in p or "projeto" in p:
        extras.append(
            "EasyData Inteligencia Urbana em Saneamento objetivo contexto justificativa mercado imobiliario saneamento basico"
        )

    if "objetivo" in p:
        extras.append(
            "objetivo coletar organizar tratar apresentar dados publicos saneamento basico municipios brasileiros"
        )

    if "contexto" in p:
        extras.append(
            "contexto saneamento basico municipios brasileiros infraestrutura urbana mercado imobiliario"
        )

    if "justificativa" in p:
        extras.append(
            "justificativa dados publicos saneamento basico auxiliar construtoras imobiliarias decisao estrategica"
        )

    return pergunta + "\n" + "\n".join(extras)


def montar_contexto(pergunta):
    consulta = expandir_pergunta(pergunta)
    resultados = vector_db.similarity_search(consulta, k=6)

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

        texto = doc.page_content.strip()
        if texto:
            contexto.append(texto)

    texto_contexto = "\n\n".join(contexto)

    print(f"Fontes usadas: {fontes[:3]}")
    print(f"Trecho de contexto: {texto_contexto[:800]}")

    return texto_contexto[:4500], fontes[:3]


def gerar_resposta(pergunta, contexto):
    if not contexto.strip():
        return "Nao encontrei essa informacao na documentacao do projeto."

    prompt = f"""
Voce e o Assistente EasyData, chatbot do site institucional da EasyData.

Responda em portugues do Brasil, de forma clara, objetiva e amigavel.
Use somente as informacoes do contexto abaixo.
Nao invente nomes, numeros, cargos, liderancas, tecnologias ou promessas.
Se a resposta nao estiver no contexto, diga apenas:
"Nao encontrei essa informacao na documentacao do projeto."

Atencao:
- A equipe EasyData tem exatamente 6 integrantes.
- Nao separe "Joao Vitor Gomes De Melo" em duas pessoas.
- Nao diga que existe lider da equipe, a menos que isso esteja explicitamente no contexto.

Contexto:
{contexto}

Pergunta:
{pergunta}

Resposta:
"""

    resposta = llm.invoke(prompt)
    return resposta.content.strip()


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

    if pergunta_saudacao(pergunta):
        return {
            "resposta": "Ola! Sou o Assistente EasyData. Posso responder perguntas sobre o projeto EasyData, objetivo, contexto, justificativa, saneamento e integrantes.",
            "fontes": [],
        }

    if pergunta_sobre_integrantes(pergunta):
        return {
            "resposta": resposta_integrantes(),
            "fontes": [
                {
                    "arquivo": "Documentacao_RAG_EasyData_Resumida.pdf",
                    "pagina": 1,
                }
            ],
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