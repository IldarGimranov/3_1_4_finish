package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleService roleService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User findByUsername(String username) { //обертка над методом репозитория, что бы не обращаться напрямую
        return userRepository.findByUsername(username);
    }


    @Transactional(readOnly = true)
    @Override
    public List<User> readAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public User readUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional
    @Override
    public void saveUser(User user, List<String> roles) {
        user.setAuthorities(roleService.getRoles(roles));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUser(User user, List<String> roles) {
        try {
            User user1 = readUserById(user.getId());
            user1.setUsername(user.getUsername());
            user1.setLastName(user.getLastName());
            user1.setYear(user.getYear());
            user1.setEmail(user.getEmail());
            String actualPassword = user1.getPassword();
            String newPassword = user.getPassword();
            if (!actualPassword.equals(newPassword)) {
                user1.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }
            user1.setAuthorities(roleService.getRoles(roles));
            userRepository.save(user1);
        } catch (NullPointerException e) {
            throw new EntityNotFoundException();
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

