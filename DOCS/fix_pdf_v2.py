import os
import re
import json
import base64
import urllib.request

DOCS_DIR = "/home/iris/slam/ObsiLock/DOCS"
IMG_DIR = os.path.join(DOCS_DIR, "images")

if not os.path.exists(IMG_DIR):
    os.makedirs(IMG_DIR)

def get_mermaid_url(mermaid_code):
    obj = {
        "code": mermaid_code,
        "mermaid": {"theme": "default"}
    }
    j = json.dumps(obj).encode("utf-8")
    b64 = base64.urlsafe_b64encode(j).decode("utf-8")
    return f"https://mermaid.ink/img/{b64}"

def process_file(filename):
    path = os.path.join(DOCS_DIR, filename)
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    pattern = re.compile(r"```mermaid\s*\n(.*?)\n```", re.DOTALL)
    matches = list(pattern.finditer(content))
    
    if not matches:
        return

    new_content = content
    
    for i, match in enumerate(matches):
        code = match.group(1).strip()
        url = get_mermaid_url(code)
        
        img_filename = f"{filename}_{i}.png"
        img_path = os.path.join(IMG_DIR, img_filename)
        
        print(f"Downloading {img_filename}...")
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req) as response:
                with open(img_path, 'wb') as out_file:
                    out_file.write(response.read())
            
            replacement = f"![Diagramme](./images/{img_filename})"
            new_content = new_content.replace(match.group(0), replacement)
        except Exception as e:
            print(f"Error downloading {img_filename}: {e}")

    out_path = path.replace(".md", ".local_img.md")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(new_content)

for f in os.listdir(DOCS_DIR):
    if f.endswith(".md") and not f.endswith(".local_img.md") and not f.endswith(".pdf_ready.md"):
        process_file(f)
