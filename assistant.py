import os
from flask import Flask, render_template, request
import openai
import PyPDF2

app = Flask(__name__)

# Set your OpenAI API key from Render environment variable
openai.api_key = os.environ.get("OPENAI_API_KEY")

@app.route("/", methods=["GET", "POST"])
def index():
    answer = ""
    if request.method == "POST":
        pdf_file = request.files["pdf"]
        question = request.form.get("question")

        # Read PDF text
        reader = PyPDF2.PdfReader(pdf_file)
        text = ""
        for page in reader.pages:
            text += page.extract_text()

        # Ask OpenAI
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[{"role":"user", "content": f"Text: {text}\nQuestion: {question}"}]
        )
        answer = response.choices[0].message.content

    return render_template("index.html", answer=answer)

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)
