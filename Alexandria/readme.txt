/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 3 SuperDuo: Alexandria*
*   ================================================
*
*        from : 1th JUL 2015
*        to : 22 AUG 2015
*
*     Kwanghyun JUNG
*     ihangulo@gmail.com
*
*    Android Devlelopment Nanodegree
*    Udacity
*
 

* support ISBN-13 
* support 2 pane mode for tablet
* you can select start page on Settings menu
* you can select internal barcode scanner or external barcode scanner on Settings menu


27 AUG 2015
-----------
Fixed below bugs

** The bug for the UX feedback - "The app could use some work. Sometimes when I add a book and don’t
double-check the ISBN, it just disappears!” isn't fixed and can be reproduced as follows:
   Add a valid ISBN code, say 1234567897, let the book detail load.
   Then, add another digit, say 2 to the already typed ISBN code, now at this point since the ISBN
   is invalid, the book details are also cleared. The user might have entered that extra digit
   accidentally, so ideally, whenever a book's detail is loaded let it stay on the screen until
   a new valid ISBN is entered.

   The solution is as simple as commenting out a single line of code.


** After adding a book with no authors, when tried to view from list of books, the App crashes!
(example ISBN: 978-0-12345678-6)

*/