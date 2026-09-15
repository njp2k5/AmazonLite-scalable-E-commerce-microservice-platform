-- Replace all Unsplash images with real book covers from Open Library CDN
-- Open Library: https://covers.openlibrary.org/b/isbn/{ISBN}-L.jpg
-- Using well-known ISBNs (Penguin Classics / widely distributed editions)

UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780156907392-L.jpg'  WHERE name = 'To the Lighthouse';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780142437247-L.jpg'  WHERE name = 'Moby-Dick';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9781400079988-L.jpg'  WHERE name = 'War and Peace';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780140268867-L.jpg'  WHERE name = 'The Odyssey';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780374528379-L.jpg'  WHERE name = 'The Brothers Karamazov';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780140449136-L.jpg'  WHERE name = 'Crime and Punishment';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780140449129-L.jpg'  WHERE name = 'Madame Bovary';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780142437223-L.jpg'  WHERE name = 'The Divine Comedy';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780140445923-L.jpg'  WHERE name = 'The Iliad';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780060934347-L.jpg'  WHERE name = 'Don Quixote';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780060883287-L.jpg'  WHERE name = 'One Hundred Years of Solitude';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780684833392-L.jpg'  WHERE name = 'Catch-22';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9781400033416-L.jpg'  WHERE name = 'Beloved';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141441146-L.jpg'  WHERE name = 'Jane Eyre';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439556-L.jpg'  WHERE name = 'Wuthering Heights';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439563-L.jpg'  WHERE name = 'Great Expectations';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780143039433-L.jpg'  WHERE name = 'The Grapes of Wrath';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439471-L.jpg'  WHERE name = 'Frankenstein';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439976-L.jpg'  WHERE name = 'Dracula';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439570-L.jpg'  WHERE name = 'The Picture of Dorian Gray';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780060850524-L.jpg'  WHERE name = 'Brave New World';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9781451673319-L.jpg'  WHERE name = 'Fahrenheit 451';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780618640157-L.jpg'  WHERE name = 'The Lord of the Rings';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439761-L.jpg'  WHERE name = 'Alice''s Adventures in Wonderland';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg'  WHERE name = 'The Little Prince';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780385333481-L.jpg'  WHERE name = 'Slaughterhouse-Five';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780385490818-L.jpg'  WHERE name = 'The Handmaid''s Tale';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780060837020-L.jpg'  WHERE name = 'The Bell Jar';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780679720201-L.jpg'  WHERE name = 'The Stranger';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439600-L.jpg'  WHERE name = 'A Tale of Two Cities';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780451419439-L.jpg'  WHERE name = 'Les Mis??rables';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780143035008-L.jpg'  WHERE name = 'Anna Karenina';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780300186963-L.jpg'  WHERE name = 'In Search of Lost Time';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780805210408-L.jpg'  WHERE name = 'The Trial';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780393979428-L.jpg'  WHERE name = 'The Metamorphosis';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780393926361-L.jpg'  WHERE name = 'Heart of Darkness';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439495-L.jpg'  WHERE name = 'Gulliver''s Travels';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780141439587-L.jpg'  WHERE name = 'Robinson Crusoe';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780140449266-L.jpg'  WHERE name = 'The Count of Monte Cristo';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780743297332-L.jpg'  WHERE name = 'The Sun Also Rises';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780684801223-L.jpg'  WHERE name = 'The Old Man and the Sea';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9781250064226-L.jpg'  WHERE name = 'The Call of the Wild';
UPDATE books SET image_url = 'https://covers.openlibrary.org/b/isbn/9780142437056-L.jpg'  WHERE name = 'The Secret Garden';

-- Verify: show all books that still use unsplash (should be 0)
SELECT COUNT(*) AS remaining_unsplash FROM books WHERE image_url LIKE '%unsplash%';

-- Show the updated books
SELECT id, name, image_url FROM books WHERE image_url LIKE '%openlibrary%' ORDER BY id;
