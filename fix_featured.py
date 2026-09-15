import psycopg2

conn = psycopg2.connect(
    "postgresql://neondb_owner:npg_Chu10NovqHGi@ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb?sslmode=require"
)
c = conn.cursor()

# Reset all featured flags
c.execute("UPDATE Books SET is_featured=false WHERE is_featured=true;")
print(f"Reset {c.rowcount} featured books")

# Set The Odyssey as featured (good unsplash image)
c.execute("UPDATE Books SET is_featured=true WHERE name='The Odyssey';")
print(f"Set {c.rowcount} books as featured")

conn.commit()

# Show state
c.execute("SELECT id, name, is_featured, is_bestseller FROM Books WHERE is_featured=true OR is_bestseller=true ORDER BY id;")
print("\nFeatured / Bestseller books:")
for r in c.fetchall():
    print(f"  id={r[0]:3d}  featured={r[2]}  bestseller={r[3]}  name={r[1]}")

c.close()
conn.close()
print("\nDone!")
