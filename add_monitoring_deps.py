import glob

deps_to_add = """        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
"""

for pom in glob.glob("e:/my_codes/FAANG-projects/AmazonLite/services/*/pom.xml"):
    with open(pom, "r", encoding="utf-8") as f:
        content = f.read()
    
    if "spring-boot-starter-actuator" in content:
        print(f"Skipping {pom}, already has actuator")
        continue

    # We want to insert right before the main </dependencies> block.
    # We can split by "</dependencies>" and insert before the first one that is followed by <dependencyManagement> or <build>
    # Actually, the main <dependencies> block is the first one closed.
    
    idx = content.find("</dependencies>")
    if idx != -1:
        new_content = content[:idx] + deps_to_add + content[idx:]
        with open(pom, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"Updated {pom}")
    else:
        print(f"Could not find </dependencies> in {pom}")
