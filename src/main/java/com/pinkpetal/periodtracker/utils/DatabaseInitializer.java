package com.pinkpetal.periodtracker.utils;

import com.pinkpetal.periodtracker.models.*;
import com.pinkpetal.periodtracker.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private com.pinkpetal.periodtracker.services.TrackingService trackingService;

    @Override
    public void run(String... args) throws Exception {
        // Clean up "admin" user from "users" table if it exists
        if (userRepository.existsById("admin")) {
            userRepository.deleteById("admin");
            System.out.println("Removed legacy admin from users table.");
        }

        // 1. Initialize Admins Table
        if (adminRepository.count() == 0) {
            String adminHashedPassword = PasswordHasher.hashPassword("admin123");
            Admin adminObj = new Admin("admin", adminHashedPassword);
            adminRepository.save(adminObj);
            System.out.println("Admins table initialized with default admin.");
        }

        // 2. Initialize Users with Menstrual Cycle History
        boolean isSeededCorrectly = userRepository.findById("rutuja")
                .map(u -> LocalDate.of(2026, 4, 2).equals(u.getRegistrationDate()))
                .orElse(false) && postRepository.count() >= 6;

        if (!isSeededCorrectly) {
            commentRepository.deleteAll();
            postRepository.deleteAll();
            reminderRepository.deleteAll();
            symptomRepository.deleteAll();
            cycleRepository.deleteAll();
            userRepository.deleteAll();
            System.out.println("Forcing database clean and seed for dummy data...");

            String defaultUserHashedPassword = PasswordHasher.hashPassword("user123");

            // Seed Users with Maharashtrian and Indian Names & distributed Registration Dates
            User u1 = new User("aditi", "Aditi Patil", defaultUserHashedPassword, 28, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 3, 12)); // Registered March
            User u2 = new User("sneha", "Sneha Deshmukh", defaultUserHashedPassword, 29, LocalDate.of(2026, 5, 12), LocalDate.of(2026, 3, 10)); // Registered March
            User u3 = new User("prachi", "Prachi Kulkarni", defaultUserHashedPassword, 29, LocalDate.of(2026, 5, 8), LocalDate.of(2026, 3, 8)); // Registered March
            User u4 = new User("rutuja", "Rutuja Shinde", defaultUserHashedPassword, 30, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 4, 2)); // Registered April
            User u5 = new User("sakshi", "Sakshi Pawar", defaultUserHashedPassword, 29, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 4, 5)); // Registered April, Irregular Cycle
            User u6 = new User("vaishnavi", "Vaishnavi Jadhav", defaultUserHashedPassword, 29, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 4, 8)); // Registered April
            User u7 = new User("tejaswini", "Tejaswini More", defaultUserHashedPassword, 29, LocalDate.of(2026, 5, 9), LocalDate.of(2026, 5, 2)); // Registered May
            User u8 = new User("pooja", "Pooja Kale", defaultUserHashedPassword, 29, LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 5)); // Registered May

            userRepository.saveAll(Arrays.asList(u1, u2, u3, u4, u5, u6, u7, u8));
            System.out.println("8 Indian/Maharashtrian Users seeded.");

            // 3. Seed Cycle History for each user
            // Aditi Patil Cycles (Registered March - 3 cycles)
            cycleRepository.save(new Cycle("aditi", LocalDate.of(2026, 3, 15), 28, "Cramps, Fatigue, Mood Swings"));
            cycleRepository.save(new Cycle("aditi", LocalDate.of(2026, 4, 12), 28, "Bloating, Headache"));
            cycleRepository.save(new Cycle("aditi", LocalDate.of(2026, 5, 10), 28, "Cramps, Mood Swings"));

            // Sneha Deshmukh Cycles (Registered March - 3 cycles)
            cycleRepository.save(new Cycle("sneha", LocalDate.of(2026, 3, 14), 29, "Cramps, Headache"));
            cycleRepository.save(new Cycle("sneha", LocalDate.of(2026, 4, 12), 30, "Fatigue, Bloating"));
            cycleRepository.save(new Cycle("sneha", LocalDate.of(2026, 5, 12), 29, "Cramps"));

            // Prachi Kulkarni Cycles (Registered March - 3 cycles)
            cycleRepository.save(new Cycle("prachi", LocalDate.of(2026, 3, 10), 29, "Mood Swings, Fatigue"));
            cycleRepository.save(new Cycle("prachi", LocalDate.of(2026, 4, 8), 30, "Cramps, Headache"));
            cycleRepository.save(new Cycle("prachi", LocalDate.of(2026, 5, 8), 29, "Bloating"));

            // Rutuja Shinde Cycles (Registered April - 2 cycles)
            cycleRepository.save(new Cycle("rutuja", LocalDate.of(2026, 4, 5), 30, "Cramps, Fatigue"));
            cycleRepository.save(new Cycle("rutuja", LocalDate.of(2026, 5, 5), 30, "Mood Swings"));

            // Sakshi Pawar Cycles (Registered April - 2 cycles, Varying lengths: 33, 26 to test irregularity flag)
            cycleRepository.save(new Cycle("sakshi", LocalDate.of(2026, 4, 8), 33, "Backache, Bloating, Acne"));
            cycleRepository.save(new Cycle("sakshi", LocalDate.of(2026, 5, 11), 26, "Headache, Fatigue"));

            // Vaishnavi Jadhav Cycles (Registered April - 2 cycles)
            cycleRepository.save(new Cycle("vaishnavi", LocalDate.of(2026, 4, 11), 30, "Headache, Cramps"));
            cycleRepository.save(new Cycle("vaishnavi", LocalDate.of(2026, 5, 11), 29, "Fatigue"));

            // Tejaswini More Cycles (Registered May - 1 cycle)
            cycleRepository.save(new Cycle("tejaswini", LocalDate.of(2026, 5, 9), 29, "Food Cravings"));

            // Pooja Kale Cycles (Registered May - 1 cycle)
            cycleRepository.save(new Cycle("pooja", LocalDate.of(2026, 5, 14), 29, "Mood Swings"));

            System.out.println("Cycle history records seeded for all users.");

            // 4. Seed Reminders
            reminderRepository.save(new Reminder("aditi", "Medicine", "09:00 AM", "Active"));
            reminderRepository.save(new Reminder("aditi", "Water", "02:00 PM", "Active"));
            reminderRepository.save(new Reminder("sneha", "Pad/Tampon", "01:00 PM", "Active"));
            reminderRepository.save(new Reminder("sakshi", "Doctor", "May 25, 05:00 PM", "Active"));
            reminderRepository.save(new Reminder("sakshi", "Medicine", "08:00 AM", "Active"));
            System.out.println("Sample user reminders seeded.");

            // 5. Seed Symptoms
            symptomRepository.save(new Symptom("aditi", LocalDate.now(), "Cramps, Fatigue, Mood Swings"));
            symptomRepository.save(new Symptom("sneha", LocalDate.now(), "Bloating, Headache"));
            symptomRepository.save(new Symptom("sakshi", LocalDate.now().minusDays(1), "Cramps, Bloating"));
            symptomRepository.save(new Symptom("rutuja", LocalDate.now().minusDays(2), "Fatigue, Backache"));
            System.out.println("Sample user symptoms seeded.");

            // 6. Seed Forum Posts and Comments in the correct sections
            // Experiences section
            Post p1 = new Post("User3072", "Hello everyone! Has anyone tried drinking spearmint tea for hormone balance and reducing acne? Share your experiences!", LocalDateTime.now().minusDays(3), "Experiences");
            postRepository.save(p1);
            commentRepository.save(new Comment(p1.getPostId(), "Yes, I drink it daily! It has really helped clear my skin and balance my cycle.", LocalDateTime.now().minusDays(3).plusHours(2), "User1234"));
            commentRepository.save(new Comment(p1.getPostId(), "I've heard it works well, but consult your doctor first to make sure it is suitable.", LocalDateTime.now().minusDays(3).plusHours(4), "User9281"));

            // PCOS section
            Post p2 = new Post("User7890", "I was recently diagnosed with PCOS. I'm feeling a bit overwhelmed. Any recommendations on diet, lifestyle, or supplements that worked for you?", LocalDateTime.now().minusDays(2), "PCOS");
            postRepository.save(p2);
            commentRepository.save(new Comment(p2.getPostId(), "Reducing refined sugar and focusing on strength training. It helped regulate my periods.", LocalDateTime.now().minusDays(2).plusHours(1), "User1234"));
            commentRepository.save(new Comment(p2.getPostId(), "Don't worry, you are not alone! It is completely manageable with small lifestyle tweaks.", LocalDateTime.now().minusDays(2).plusHours(3), "User8192"));
            commentRepository.save(new Comment(p2.getPostId(), "Yes, focus on low-glycemic foods. And remember to be patient with your body.", LocalDateTime.now().minusDays(2).plusHours(4), "User9281"));

            Post p3 = new Post("User1234", "Does anyone else suffer from severe fatigue and mood swings during their luteal phase? PCOS makes it so much worse.", LocalDateTime.now().minusDays(1).plusHours(2), "PCOS");
            postRepository.save(p3);
            commentRepository.save(new Comment(p3.getPostId(), "Yes, absolutely. I started taking Vitamin D and Magnesium supplements after consulting my doctor, and it helped significantly.", LocalDateTime.now().minusDays(1).plusHours(4), "User4567"));
            commentRepository.save(new Comment(p3.getPostId(), "Same here. I use the breathing exercise in the Self-Care section when the mood swings get intense. It really grounds me.", LocalDateTime.now().minusDays(1).plusHours(5), "User7890"));

            // Doubts section
            Post p4 = new Post("User4567", "My cycle length has been varying from 26 days to 33 days lately. Does anyone else experience cycle irregularities like this? Should I consult a doctor?", LocalDateTime.now().minusDays(1), "Doubts");
            postRepository.save(p4);
            commentRepository.save(new Comment(p4.getPostId(), "Yes! If the variation is more than 5-7 days, it's a good idea to check with a gynecologist just to be sure.", LocalDateTime.now().minusDays(1).plusHours(2), "User3072"));
            commentRepository.save(new Comment(p4.getPostId(), "I had a similar issue last year. Tracking my stress levels really helped me see a pattern. Try to get enough sleep!", LocalDateTime.now().minusDays(1).plusHours(3), "User1234"));

            Post p5 = new Post("User2891", "Is it normal to have mild spotting a few days before the period starts? It's my first time noticing it.", LocalDateTime.now().minusHours(18), "Doubts");
            postRepository.save(p5);
            commentRepository.save(new Comment(p5.getPostId(), "Yes, it can be normal (ovulation spotting or mild hormonal drop), but if it continues, it's best to consult a professional.", LocalDateTime.now().minusHours(16), "User7890"));
            commentRepository.save(new Comment(p5.getPostId(), "Agree. Keep tracking the color and flow rate on your PinkPetal dashboard so you have data to show your doctor.", LocalDateTime.now().minusHours(14), "User3072"));

            // Myths vs Facts section
            Post p6 = new Post("User9281", "Myth: You cannot get pregnant during your period.\nFact: Yes, you can! Sperm can survive inside the female body for up to 5 days, so if you have a shorter cycle, ovulation can happen soon after your period ends.", LocalDateTime.now().minusHours(10), "Myths vs Facts");
            postRepository.save(p6);
            commentRepository.save(new Comment(p6.getPostId(), "Wow, I didn't know sperm could survive that long! Thanks for sharing this fact.", LocalDateTime.now().minusHours(8), "User8291"));
            commentRepository.save(new Comment(p6.getPostId(), "This is why tracking fertile windows dynamically is so important. PinkPetal does this very well!", LocalDateTime.now().minusHours(6), "User1234"));

            System.out.println("Forum posts and comments seeded.");
        }

        // Recalculate cycle lengths and sync predicted cycle length & latest period date for all users on startup
        try {
            for (User user : userRepository.findAll()) {
                if (!"admin".equals(user.getUserId())) {
                    trackingService.recalculateAndStoreCycleLengths(user.getUserId());
                }
            }
            System.out.println("Cycle lengths recalculated and stored on startup for all users.");
        } catch (Exception e) {
            System.err.println("Error recalculating cycle lengths on startup: " + e.getMessage());
        }
    }
}
