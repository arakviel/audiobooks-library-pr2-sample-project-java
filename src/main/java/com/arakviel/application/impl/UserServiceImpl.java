package com.arakviel.application.impl;

import com.arakviel.application.contract.UserService;
import com.arakviel.application.exception.MultiFieldValidationException;
import com.arakviel.application.exception.ValidationException;
import com.arakviel.application.validation.ValidationHelper;
import com.arakviel.domain.entities.Collection;
import com.arakviel.domain.entities.ListeningProgress;
import com.arakviel.domain.entities.User;
import com.arakviel.infrastructure.file.FileStorageService;
import com.arakviel.infrastructure.persistence.PersistenceContext;
import com.arakviel.infrastructure.persistence.contract.CollectionRepository;
import com.arakviel.infrastructure.persistence.contract.ListeningProgressRepository;
import com.arakviel.infrastructure.persistence.contract.UserRepository;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реалізація сервісу для управління користувачами.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final ListeningProgressRepository listeningProgressRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;

    public UserServiceImpl(UserRepository userRepository,
                          CollectionRepository collectionRepository,
                          ListeningProgressRepository listeningProgressRepository,
                          PersistenceContext persistenceContext,
                          FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.collectionRepository = collectionRepository;
        this.listeningProgressRepository = listeningProgressRepository;
        this.persistenceContext = persistenceContext;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Створює нового користувача та, за потреби, завантажує аватар.
     */
    @Override
    public User create(User user, InputStream avatar, String avatarName) {
        validateUser(user);
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }

        // Перевірка на дублювання username та email
        ValidationHelper duplicateValidator = new ValidationHelper();
        if (existsByUsername(user.getUsername())) {
            duplicateValidator.addError("username", "користувач з таким ім'ям уже існує");
        }
        if (existsByEmail(user.getEmail())) {
            duplicateValidator.addError("email", "користувач з таким email уже існує");
        }
        duplicateValidator.throwIfHasErrors();

        // Хешування пароля
        user.setPasswordHash(hashPassword(user.getPasswordHash()));

        // Обробка завантаження аватара
        if (avatar != null && avatarName != null) {
            Path avatarPath = fileStorageService.save(avatar, avatarName, user.getId());
            user.setAvatarPath(avatarPath.toString());
        }

        persistenceContext.registerNew(user);
        persistenceContext.commit();
        return user;
    }

    /**
     * Оновлює існуючого користувача та, за потреби, оновлює аватар.
     */
    @Override
    public User update(UUID id, User user, InputStream avatar, String avatarName) {
        validateUser(user);
        user.setId(id);

        // Перевірка існування користувача
        Optional<User> existingUserOpt = userRepository.findById(id);
        ValidationHelper updateValidator = new ValidationHelper();

        if (!existingUserOpt.isPresent()) {
            updateValidator.addError("id", "користувач з таким ідентифікатором не існує");
            updateValidator.throwIfHasErrors();
        }

        User existingUser = existingUserOpt.get();

        // Перевірка на дублювання username (якщо змінюється)
        if (!existingUser.getUsername().equals(user.getUsername()) && existsByUsername(user.getUsername())) {
            updateValidator.addError("username", "користувач з таким ім'ям уже існує");
        }

        // Перевірка на дублювання email (якщо змінюється)
        if (!existingUser.getEmail().equals(user.getEmail()) && existsByEmail(user.getEmail())) {
            updateValidator.addError("email", "користувач з таким email уже існує");
        }

        updateValidator.throwIfHasErrors();

        // Хешування пароля, якщо він змінився
        if (!user.getPasswordHash().equals(existingUser.getPasswordHash())) {
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
        }

        // Обробка існуючого аватара
        if (existingUser.getAvatarPath() != null && avatar != null && avatarName != null) {
            String fileName = Path.of(existingUser.getAvatarPath()).getFileName().toString();
            fileStorageService.delete(fileName, id);
        }

        // Обробка нового аватара
        if (avatar != null && avatarName != null) {
            Path avatarPath = fileStorageService.save(avatar, avatarName, id);
            user.setAvatarPath(avatarPath.toString());
        } else if (existingUser.getAvatarPath() != null) {
            user.setAvatarPath(existingUser.getAvatarPath());
        }

        persistenceContext.registerUpdated(id, user);
        persistenceContext.commit();
        return user;
    }

    /**
     * Видаляє користувача та пов'язаний аватар.
     */
    @Override
    public void delete(UUID id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Видалення аватара
            if (user.getAvatarPath() != null) {
                String fileName = Path.of(user.getAvatarPath()).getFileName().toString();
                fileStorageService.delete(fileName, id);
            }

            persistenceContext.registerDeleted(user);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll(int offset, int limit) {
        validatePagination(offset, limit);
        return userRepository.findAll(offset, limit);
    }

    @Override
    public List<User> findByUsername(String username) {
        new ValidationHelper()
                .notEmpty("username", username)
                .throwIfHasErrors();
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findByEmail(String email) {
        new ValidationHelper()
                .notEmpty("email", email)
                .throwIfHasErrors();
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<Collection> findCollectionsByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return collectionRepository.findByUserId(userId);
    }

    @Override
    public List<ListeningProgress> findListeningProgressByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return listeningProgressRepository.findByUserId(userId);
    }

    @Override
    public long countCollectionsByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return collectionRepository.countByUserId(userId);
    }

    @Override
    public long countListeningProgressByUserId(UUID userId) {
        new ValidationHelper()
                .validUuid("userId", userId)
                .throwIfHasErrors();
        return listeningProgressRepository.countByUserId(userId);
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        ValidationHelper passwordValidator = new ValidationHelper()
                .validUuid("userId", userId)
                .notEmpty("oldPassword", oldPassword)
                .notEmpty("newPassword", newPassword);

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            passwordValidator.addError("userId", "користувач не знайдений");
        }

        passwordValidator.throwIfHasErrors();

        User user = userOpt.get();
        String hashedOldPassword = hashPassword(oldPassword);

        if (!user.getPasswordHash().equals(hashedOldPassword)) {
            new ValidationHelper()
                    .addError("oldPassword", "неправильний старий пароль")
                    .throwIfHasErrors();
        }

        user.setPasswordHash(hashPassword(newPassword));
        persistenceContext.registerUpdated(userId, user);
        persistenceContext.commit();
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        new ValidationHelper()
                .notEmpty("username", username)
                .notEmpty("password", password)
                .throwIfHasErrors();

        List<User> users = findByUsername(username);
        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        String hashedPassword = hashPassword(password);
        
        if (user.getPasswordHash().equals(hashedPassword)) {
            return Optional.of(user);
        }
        
        return Optional.empty();
    }

    /**
     * Валідує дані користувача з використанням нового підходу.
     */
    private void validateUser(User user) {
        ValidationHelper validator = new ValidationHelper()
                .notNull("user", user);

        if (user != null) {
            validator
                    .notEmpty("username", user.getUsername())
                    .validUsername("username", user.getUsername())
                    .notEmpty("passwordHash", user.getPasswordHash())
                    .notEmpty("email", user.getEmail())
                    .validEmail("email", user.getEmail());
        }

        validator.throwIfHasErrors();
    }

    /**
     * Валідує параметри пагінації з використанням нового підходу.
     */
    private void validatePagination(int offset, int limit) {
        new ValidationHelper()
                .nonNegative("offset", offset)
                .positive("limit", limit)
                .throwIfHasErrors();
    }

    /**
     * Хешує пароль за допомогою SHA-256.
     */
    String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Помилка хешування пароля", e);
        }
    }
}
