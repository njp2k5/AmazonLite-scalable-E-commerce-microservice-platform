import random

additional_books = [
    ("To the Lighthouse", "Virginia Woolf's masterpiece of modernist literature.", 12.50, "Virginia Woolf", "fiction"),
    ("Moby-Dick", "The epic tale of Captain Ahab's obsessive quest.", 14.20, "Herman Melville", "fiction"),
    ("War and Peace", "A broad panorama of Russian life during the Napoleonic era.", 25.00, "Leo Tolstoy", "fiction"),
    ("The Odyssey", "Homer's epic poem of Odysseus's journey home.", 10.99, "Homer", "fiction"),
    ("The Brothers Karamazov", "A passionate philosophical novel by Dostoevsky.", 18.50, "Fyodor Dostoevsky", "fiction"),
    ("Crime and Punishment", "A novel about the mental anguish of Rodion Raskolnikov.", 15.00, "Fyodor Dostoevsky", "fiction"),
    ("Madame Bovary", "Gustave Flaubert's story of Emma Bovary.", 11.20, "Gustave Flaubert", "fiction"),
    ("The Divine Comedy", "Dante's journey through Hell, Purgatory, and Paradise.", 19.99, "Dante Alighieri", "poetry"),
    ("The Iliad", "Homer's epic about the Trojan War.", 11.50, "Homer", "poetry"),
    ("Don Quixote", "Miguel de Cervantes's classic tale of a delusional knight.", 16.75, "Miguel de Cervantes", "fiction"),
    ("One Hundred Years of Solitude", "Gabriel García Márquez's magical realist epic.", 14.50, "Gabriel Garcia Marquez", "fiction"),
    ("The Sound and the Fury", "William Faulkner's modernist novel.", 13.00, "William Faulkner", "fiction"),
    ("Catch-22", "Joseph Heller's satirical war novel.", 12.99, "Joseph Heller", "fiction"),
    ("Beloved", "Toni Morrison's novel about a former slave.", 14.00, "Toni Morrison", "fiction"),
    ("Jane Eyre", "Charlotte Brontë's novel of romance and independence.", 9.50, "Charlotte Bronte", "fiction"),
    ("Wuthering Heights", "Emily Brontë's tale of doomed love.", 8.99, "Emily Bronte", "fiction"),
    ("Great Expectations", "Charles Dickens's coming-of-age story.", 10.00, "Charles Dickens", "fiction"),
    ("The Grapes of Wrath", "John Steinbeck's novel of the Great Depression.", 13.50, "John Steinbeck", "fiction"),
    ("Invisible Man", "Ralph Ellison's novel about an unnamed African American protagonist.", 15.50, "Ralph Ellison", "fiction"),
    ("Ulysses", "James Joyce's modernist masterpiece.", 18.00, "James Joyce", "fiction"),
    ("Frankenstein", "Mary Shelley's classic Gothic novel.", 8.50, "Mary Shelley", "science fiction"),
    ("Dracula", "Bram Stoker's seminal vampire novel.", 9.00, "Bram Stoker", "horror"),
    ("The Picture of Dorian Gray", "Oscar Wilde's philosophical novel.", 10.50, "Oscar Wilde", "fiction"),
    ("Brave New World", "Aldous Huxley's dystopian classic.", 12.00, "Aldous Huxley", "science fiction"),
    ("Fahrenheit 451", "Ray Bradbury's novel about a future without books.", 11.50, "Ray Bradbury", "science fiction"),
    ("The Lord of the Rings", "J.R.R. Tolkien's high fantasy epic.", 29.99, "J.R.R. Tolkien", "fantasy"),
    ("Harry Potter and the Sorcerer's Stone", "J.K. Rowling's magical tale.", 10.99, "J.K. Rowling", "fantasy"),
    ("The Chronicles of Narnia", "C.S. Lewis's beloved fantasy series.", 25.00, "C.S. Lewis", "fantasy"),
    ("Alice's Adventures in Wonderland", "Lewis Carroll's surreal children's book.", 7.99, "Lewis Carroll", "fantasy"),
    ("The Little Prince", "Antoine de Saint-Exupéry's philosophical novella.", 8.99, "Antoine de Saint-Exupery", "children"),
    ("Slaughterhouse-Five", "Kurt Vonnegut's anti-war novel.", 13.99, "Kurt Vonnegut", "fiction"),
    ("The Handmaid's Tale", "Margaret Atwood's dystopian novel.", 14.99, "Margaret Atwood", "fiction"),
    ("The Bell Jar", "Sylvia Plath's semi-autobiographical novel.", 11.99, "Sylvia Plath", "fiction"),
    ("The Catcher in the Rye", "J.D. Salinger's classic of teenage rebellion.", 10.99, "J.D. Salinger", "fiction"),
    ("The Stranger", "Albert Camus's existentialist novel.", 9.99, "Albert Camus", "fiction"),
    ("A Tale of Two Cities", "Charles Dickens's historical novel.", 8.99, "Charles Dickens", "fiction"),
    ("Les Misérables", "Victor Hugo's epic historical novel.", 19.99, "Victor Hugo", "fiction"),
    ("Anna Karenina", "Leo Tolstoy's tragic romance.", 16.50, "Leo Tolstoy", "fiction"),
    ("The Brothers Karamazov", "Fyodor Dostoevsky's philosophical novel.", 17.50, "Fyodor Dostoevsky", "fiction"),
    ("Middlemarch", "George Eliot's study of provincial life.", 14.50, "George Eliot", "fiction"),
    ("In Search of Lost Time", "Marcel Proust's monumental work.", 35.00, "Marcel Proust", "fiction"),
    ("The Trial", "Franz Kafka's surreal bureaucratic nightmare.", 10.50, "Franz Kafka", "fiction"),
    ("The Metamorphosis", "Franz Kafka's novella of transformation.", 7.50, "Franz Kafka", "fiction"),
    ("Heart of Darkness", "Joseph Conrad's novella of colonialism.", 8.50, "Joseph Conrad", "fiction"),
    ("Gulliver's Travels", "Jonathan Swift's satirical travelogue.", 9.50, "Jonathan Swift", "fiction"),
    ("Robinson Crusoe", "Daniel Defoe's castaway novel.", 8.50, "Daniel Defoe", "fiction"),
    ("The Count of Monte Cristo", "Alexandre Dumas's adventure novel.", 18.50, "Alexandre Dumas", "fiction"),
    ("Don Quixote", "Miguel de Cervantes's comic masterpiece.", 15.50, "Miguel de Cervantes", "fiction"),
    ("The Canterbury Tales", "Geoffrey Chaucer's collection of stories.", 12.50, "Geoffrey Chaucer", "poetry"),
    ("Leaves of Grass", "Walt Whitman's poetry collection.", 11.50, "Walt Whitman", "poetry"),
    ("The Sun Also Rises", "Ernest Hemingway's novel of the Lost Generation.", 12.99, "Ernest Hemingway", "fiction"),
    ("On the Road", "Jack Kerouac's Beat Generation classic.", 13.50, "Jack Kerouac", "fiction"),
    ("The Old Man and the Sea", "Ernest Hemingway's novella.", 9.99, "Ernest Hemingway", "fiction"),
    ("The Call of the Wild", "Jack London's adventure novel.", 8.99, "Jack London", "fiction"),
    ("The Secret Garden", "Frances Hodgson Burnett's children's novel.", 7.99, "Frances Hodgson Burnett", "children")
]

with open('e:/my_codes/FAANG-projects/AmazonLite/seed.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
in_books = False
for line in lines:
    if line.strip() == ']':
        if in_books:
            # Inject new books here
            for i, b in enumerate(additional_books):
                title, desc, price, author, category = b
                stock = random.randint(10, 200)
                image_url = f"https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=800&random={i}"
                isbn = f"9780{random.randint(100000000, 999999999)}"
                publisher = "Classic Books"
                format = "Paperback"
                pages = random.randint(150, 800)
                language = "English"
                new_lines.append(f'        ("{title}", "{desc}", {price}, {stock}, "{category}", "{image_url}", "{isbn}", "{author}", "{publisher}", "{format}", {pages}, "{language}", secondary_images),\n')
            in_books = False
    new_lines.append(line)
    if 'books = [' in line:
        in_books = True

with open('e:/my_codes/FAANG-projects/AmazonLite/seed.py', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
