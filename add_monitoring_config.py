import glob

config_to_add = """
management:
  endpoints:
    web:
      exposure:
        include: prometheus, health, info
  metrics:
    export:
      prometheus:
        enabled: true
"""

for yml in glob.glob("e:/my_codes/FAANG-projects/AmazonLite/services/*/src/main/resources/application.yml"):
    with open(yml, "r", encoding="utf-8") as f:
        content = f.read()
    
    if "prometheus" in content:
        print(f"Skipping {yml}, already has prometheus config")
        continue

    with open(yml, "a", encoding="utf-8") as f:
        f.write("\n" + config_to_add)
    print(f"Updated {yml}")
