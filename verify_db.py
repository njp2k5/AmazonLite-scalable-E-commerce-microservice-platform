import psycopg2

conn = psycopg2.connect(
    "postgresql://neondb_owner:npg_Chu10NovqHGi@ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require"
)
c = conn.cursor()
c.execute("SELECT COUNT(*) FROM Books;")
print(f"Total books in DB: {c.fetchone()[0]}")

c.execute("SELECT id, name FROM Books WHERE name IN ('1984', 'Dune', 'The Midnight Library') ORDER BY id;")
print("\nDeleted books still in DB?")
rows = c.fetchall()
if rows:
    for r in rows:
        print(f"  id={r[0]}  name={r[1]}")
else:
    print("  None found - correctly deleted!")

c.execute("SELECT id, name, is_featured, is_bestseller FROM Books ORDER BY id LIMIT 20;")
print("\nFirst 20 books in DB (current state):")
for r in c.fetchall():
    print(f"  id={r[0]:3d}  featured={r[2]}  bestseller={r[3]}  name={r[1]}")
c.close()
conn.close()
