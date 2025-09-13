from dotenv import load_dotenv
import os

load_dotenv()  # load .env file
openai.api_key = os.getenv("OPENAI_API_KEY")
from flask import Flask, request, jsonify, render_template
import openai
import fitz  # PyMuPDF for PDF text extraction

app = Flask(__name__)

# Set your OpenAI API key
openai.api_key = ""

# Home route
@app.route("/")
def home():
    return render_template("index.html")

# Extract text from PDF
@app.route("/upload", methods=["POST"])
def upload_pdf():
    file = request.files["file"]
    pdf_text = ""
    with fitz.open(stream=file.read(), filetype="pdf") as doc:
        for page in doc:
            pdf_text += page.get_text()
    return jsonify({"text": pdf_text})

# Ask AI with PDF text
@app.route("/ask", methods=["POST"])
def ask_question():
    data = request.json
    pdf_text = data["pdfText"]
    question = data["question"]

    prompt = f"""
    You are an assistant. Use the following PDF text to answer the question:
    PDF Content: {pdf_text}
    Question: {question}
    Answer clearly and concisely.
    """

    response = openai.ChatCompletion.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}]
    )

    answer = response["choices"][0]["message"]["content"]
    return jsonify({"answer": answer})

if __name__ == "__main__":
    app.run(debug=True)