package com.example.shop3.service;

import com.example.shop3.dto.AddBookDTO;
import com.example.shop3.model.Book;
import com.example.shop3.model.Tag; // اضافه شد
import com.example.shop3.model.User; // اضافه شد
import com.example.shop3.repository.BookRepository;
import com.example.shop3.repository.TagRepository; // اضافه شد
import com.example.shop3.repository.UserRepository; // اضافه شد (اگر owner رو جداگانه fetch می کنید)
import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.context.SecurityContextHolder; // راه دیگر گرفتن کاربر
// import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // *** مهم ***

import java.util.List; // اضافه شد
import java.util.Set; // اضافه شد
import java.util.stream.Collectors; // اضافه شد

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final TagRepository tagRepository; // <- تزریق شد
    // private final UserRepository userRepository; // اگر user رو اینجا fetch می کنید

    @Transactional // *** مهم: کل متد باید در یک تراکنش اجرا شود ***
    public Book registerBook(AddBookDTO bookDTO, String relativeCoverUrl, User owner) throws Exception { // <- User owner اضافه شد و Exception برای خطاهای احتمالی

        // اگر owner رو از کنترلر نمیگرفتیم، اینجا میگرفتیم:
        /*
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserDetails)) {
            throw new IllegalStateException("User not authenticated properly.");
        }
        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        // Fetch مجدد user از دیتابیس برای اطمینان از آپدیت بودن و مدیریت شدن توسط JPA
        User owner = userRepository.findById(userDetails.getUserEntity().getId())
                           .orElseThrow(() -> new UsernameNotFoundException("Owner user not found in DB"));
        */

        // --- ۱. ساختن Entity کتاب ---
        Book book = new Book();
        book.setName(bookDTO.getName());
        book.setPrice(bookDTO.getPrice());
        book.setNumber(bookDTO.getAvailableNum()); // <- اسم فیلد در DTO شما number بود
        book.setOwner(owner); // <- تنظیم Owner
        book.setEnabled(true); // <- مقدار پیش فرض

        // تنظیم آواتار (کاور) در صورت وجود
        if (relativeCoverUrl != null && !relativeCoverUrl.isBlank()) {
            book.setAvatar(relativeCoverUrl);
        }

        // --- ۲. مدیریت تگ ها ---
        if (bookDTO.getTagIds() != null && !bookDTO.getTagIds().isEmpty()) {
            // پیدا کردن Tag entity های موجود با ID های داده شده
            List<Tag> foundTags = tagRepository.findAllById(bookDTO.getTagIds());

            // بررسی اینکه آیا همه ID ها معتبر بودند (اختیاری)
            if(foundTags.size() != bookDTO.getTagIds().size()){
                System.err.println("Warning: Some tag IDs provided were not found in the database.");
                // می توانید Exception پرتاب کنید یا فقط تگ های پیدا شده را اضافه کنید
            }

            // اضافه کردن تگ های پیدا شده به کتاب با استفاده از متد کمکی
            // این فرض می کند که متد addTag در Book entity وجود دارد و رابطه دوطرفه را مدیریت می کند
            for (Tag tag : foundTags) {
                book.addTag(tag);
            }
            // روش جایگزین: مستقیم Set کردن (اگر متد کمکی ندارید):
            // book.setTags(new HashSet<>(foundTags)); // نیاز به import HashSet
        }

        // --- ۳. ذخیره کتاب در دیتابیس ---
        try {
            Book savedBook = bookRepository.save(book);
            System.out.println("Book saved successfully with ID: " + savedBook.getId());
            return savedBook; // <- برگرداندن کتاب ذخیره شده
        } catch (Exception e) {
            // خطاهایی مثل نقض unique constraint نام کتاب (اگر وجود داشته باشد) اینجا مشخص می شود
            System.err.println("Error saving book to database: " + e.getMessage());
            // می توانید Exception را دوباره throw کنید یا یک Exception سفارشی برگردانید
            throw new Exception("Could not save book: " + e.getMessage(), e);
        }
    }

    // (ممکن است متدهای دیگری مثل getBookById, getAllBooks و ... هم اینجا اضافه شوند)
}