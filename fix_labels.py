import os, glob, re

target_dir = "/home/iris/slam/ObsiLock/coffreFortJava-main/src/main/resources/com/coffrefort/client"

for fxml_file in glob.glob(os.path.join(target_dir, "*.fxml")):
    if os.path.basename(fxml_file) in ['main.fxml', 'login2.fxml']:
        continue

    with open(fxml_file, "r") as f:
        content = f.read()

    # We want to replace -fx-text-fill: black with -fx-text-fill: white
    # EXCEPT we might want buttons to be black. If we just blindly replace it, the lime buttons will have white text. Wait, lime button with white text is fine, but black is better.
    # Actually, the user has "-fx-text-fill: black" in Label and CheckBox
    # We can just change all "-fx-text-fill: black" to "white" EXCEPT when it's just before "-fx-background-radius" (which usually means a Button, look at share.fxml) 
    
    # Let's just globally replace -fx-text-fill: black with -fx-text-fill: white
    content = re.sub(r"-fx-text-fill:\s*black", "-fx-text-fill: white", content)
    
    # Then for the lime buttons, we can make their text black again
    content = re.sub(r"-fx-background-color:\s*#94E01E;\s*-fx-text-fill:\s*white;", "-fx-background-color: #94E01E;\n-fx-text-fill: black;", content)
    content = re.sub(r"-fx-background-color:\s*#94E01E;\r?\n\s*-fx-text-fill:\s*white", "-fx-background-color: #94E01E;\n                        -fx-text-fill: black", content)
    
    with open(fxml_file, "w") as f:
        f.write(content)
        
    print(f"Fixed {fxml_file}")

