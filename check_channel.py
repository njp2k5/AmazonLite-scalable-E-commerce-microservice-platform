import psycopg2

# Use the same exact JDBC URL format as Java
conn_str = "postgresql://neondb_owner:npg_Chu10NovqHGi@ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require&channel_binding=require"
conn = psycopg2.connect(conn_str)
c = conn.cursor()

c.execute("SELECT COUNT(*) FROM books;")
total = c.fetchone()[0]
print(f"Total books: {total}")

c.execute("SELECT id, name FROM books WHERE name IN ('1984', 'Dune', 'The Midnight Library') ORDER BY id;")
rows = c.fetchall()
print(f"\nTarget books:")
for r in rows:
    print(f"  id={r[0]}  name={r[1]}")

c.execute("SELECT id, name, is_featured FROM books ORDER BY id LIMIT 20;")
print("\nFirst 20 books (id + name):")
for r in c.fetchall():
    print(f"  id={r[0]:3d}  featured={r[2]}  name={r[1]}")

c.close()
conn.close()
