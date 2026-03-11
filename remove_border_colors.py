import os, glob, re

target_dir = "/home/iris/slam/ObsiLock/coffreFortJava-main/src/main/resources/com/coffrefort/client"

for fxml_file in glob.glob(os.path.join(target_dir, "*.fxml")):
    with open(fxml_file, "r") as f:
        content = f.read()

    # remove explicit border colors that override CSS
    content = re.sub(r"-fx-border-color:\s*#[0-9A-Fa-f]{6};\s*", "", content)
    # remove empty styles if any
    content = re.sub(r'style="\s*"', '', content)
    
    with open(fxml_file, "w") as f:
        f.write(content)

