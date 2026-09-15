import re

with open('e:/my_codes/FAANG-projects/AmazonLite/seed.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
seen_titles = set()
for line in lines:
    if '("' in line.strip() and '",' in line:
        # Extract title roughly
        match = re.search(r'\(\s*"([^"]+)"', line)
        if match:
            title = match.group(1)
            if title in seen_titles:
                continue
            seen_titles.add(title)
    new_lines.append(line)

with open('e:/my_codes/FAANG-projects/AmazonLite/seed.py', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
