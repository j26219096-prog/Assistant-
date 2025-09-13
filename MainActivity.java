package python ;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
} 
pip install flask
from flask import Flask, render_template_string, request, jsonify
import os

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = "uploads"
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

# HTML template embedded here (frontend + backend together)
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>File Q&A Assistant</title>
  <style>
    body { font-family: Arial, sans-serif; background: #f4f4f9; padding: 20px; }
    .container { max-width: 700px; margin: auto; background: #fff; padding: 20px;
                 border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
    h1 { text-align: center; }
    input, textarea, button { width: 100%; padding: 10px; margin: 10px 0;
                              border-radius: 8px; border: 1px solid #ccc; }
    button { background: #007bff; color: white; font-weight: bold; cursor: pointer; }
    button:hover { background: #0056b3; }
    .output { margin-top: 15px; padding: 10px; background: #f9f9f9;
              border-radius: 8px; border: 1px solid #ddd; }
  </style>
</head>
<body>
  <div class="container">
    <h1>📂 File Q&A Assistant</h1>

    <input type="file" id="fileInput">
    <button onclick="uploadFile()">Upload File</button>
    <div id="fileDetails" class="output"></div>

    <textarea id="question" placeholder="Ask a question about the file..."></textarea>
    <button onclick="askQuestion()">Get Answer</button>

    <div id="answer" class="output"></div>
  </div>

  <script>
    let uploadedFile = "";

    async function uploadFile() {
      const fileInput = document.getElementById("fileInput");
      const file = fileInput.files[0];
      const fileDetails = document.getElementById("fileDetails");

      if (!file) {
        fileDetails.innerHTML = "⚠️ Please select a file first.";
        return;
      }

      let formData = new FormData();
      formData.append("file", file);

      let response = await fetch("/upload", { method: "POST", body: formData });
      let result = await response.json();

      if (result.filename) {
        uploadedFile = result.filename;
        fileDetails.innerHTML = `✅ File Uploaded: ${result.filename} (${(file.size/1024).toFixed(2)} KB)`;
      } else {
        fileDetails.innerHTML = `❌ Error: ${result.error}`;
      }
    }

    async function askQuestion() {
      const question = document.getElementById("question").value;
      const answerDiv = document.getElementById("answer");

      let response = await fetch("/ask", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ question: question, filename: uploadedFile })
      });

      let result = await response.json();
      answerDiv.innerHTML = `<strong>Q:</strong> ${question}<br><strong>A:</strong> ${result.answer}`;
    }
  </script>
</body>
</html>
"""

# Home route
@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

# Upload route
@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "Empty filename"}), 400

    filepath = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
    file.save(filepath)

    return jsonify({"message": "File uploaded successfully", "filename": file.filename})

# Q&A route (currently dummy, AI can be added later)
@app.route('/ask', methods=['POST'])
def ask_question():
    data = request.json
    question = data.get("question", "")
    filename = data.get("filename", "")

    if not question.strip():
        return jsonify({"answer": "⚠️ Please provide a question."})

    # TODO: Here you can add AI logic to read the uploaded file
    # For now, just return a placeholder
    answer = f"You asked: '{question}'. (File: {filename}) -- AI answer goes here."

    return jsonify({"answer": answer})

if __name__ == '__main__':
    app.run(debug=True)