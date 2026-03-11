import os, glob, re

target_dir = "/home/iris/slam/ObsiLock/coffreFortJava-main/src/main/resources/com/coffrefort/client"

for fxml_file in glob.glob(os.path.join(target_dir, "*.fxml")):
    # we don't want to mess up main.fxml or login2.fxml as they are already styled correctly
    if os.path.basename(fxml_file) in ['main.fxml', 'login2.fxml']:
        continue

    with open(fxml_file, "r") as f:
        content = f.read()

    # Backgrounds
    content = re.sub(r"-fx-background-color:\s*#E5E5E5", "-fx-background-color: #121417", content)
    content = re.sub(r"-fx-background-color:\s*white", "-fx-background-color: #1c1f23", content)
    content = re.sub(r"-fx-background-color:\s*#f8f8f8", "-fx-background-color: #1c1f23", content)
    content = re.sub(r"-fx-background-color:\s*#980b0b", "-fx-background-color: #94E01E", content)
    content = re.sub(r"-fx-background-color:\s*#cccccc", "-fx-background-color: #32373d", content)
    
    # Borders
    content = re.sub(r"-fx-border-color:\s*#cccccc", "-fx-border-color: #2a2e33", content)
    content = re.sub(r"-fx-border-color:\s*#e0e0e0", "-fx-border-color: #2a2e33", content)

    # Texts
    content = re.sub(r"-fx-text-fill:\s*#980b0b", "-fx-text-fill: #94E01E", content)
    content = re.sub(r"-fx-text-fill:\s*#333333", "-fx-text-fill: white", content)
    content = re.sub(r"-fx-text-fill:\s*#666666", "-fx-text-fill: #9499a1", content)
    content = re.sub(r"-fx-text-fill:\s*#999999", "-fx-text-fill: #9499a1", content)
    content = re.sub(r"-fx-text-fill:\s*#777777", "-fx-text-fill: #9499a1", content)
    
    # Button texts (white -> black) when background is lime
    # If there's an element that was white text, let's keep it if the background is dark, but for buttons it was white on red. Now it's lime. So text should be black.
    # We can just let CSS override text-fill if we remove inline text-fill from buttons, but it's hard. 
    # Let's replace -fx-text-fill: white with -fx-text-fill: black for buttons only, or globally if it was primarily buttons.
    # The login and main were excluded, so in these dialogs "white" text was mostly on #980b0b buttons.
    content = re.sub(r"-fx-text-fill:\s*white", "-fx-text-fill: black", content)
    
    # Also <Text fill="#..."> elements
    content = re.sub(r'fill="#980b0b"', 'fill="#94E01E"', content)
    content = re.sub(r'fill="#333333"', 'fill="white"', content)
    content = re.sub(r'fill="#666666"', 'fill="#9499a1"', content)
    content = re.sub(r'fill="#999999"', 'fill="#9499a1"', content)

    # Shadows
    content = re.sub(r'<Color\s+red="0.6"\s+green="0.04"\s+blue="0.04"', '<Color red="0.58" green="0.88" blue="0.11"', content)

    with open(fxml_file, "w") as f:
        f.write(content)
        
    print(f"Updated {fxml_file}")
