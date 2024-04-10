
package com.parking.administration.demo.service;

import com.parking.administration.demo.domain.User;
import com.parking.administration.demo.domain.Vehicle;
import com.parking.administration.demo.domain.token.ConfirmationToken;
import com.parking.administration.demo.infra.exception.BadRequestException;
import com.parking.administration.demo.infra.exception.PasswordNotValidException;
import com.parking.administration.demo.infra.exception.UserNotFoundException;
import com.parking.administration.demo.infra.exception.VehicleNotFoundException;
import com.parking.administration.demo.infra.exception.enums.ErrorCode;
import com.parking.administration.demo.repository.UserRepository;
import com.parking.administration.demo.repository.VehicleRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.parking.administration.demo.utils.Utility.LOGGER;

@Service
public class UserService implements UserDetailsService {
    private UserRepository userRepository;
    private ConfirmationTokenService confirmationTokenService;
    private EmailService emailService;
    public UserService(UserRepository userRepository,
                       ConfirmationTokenService confirmationTokenService, EmailService emailService) {
        this.userRepository = userRepository;
        this.confirmationTokenService = confirmationTokenService;
        this.emailService = emailService;
    }
    public UserService() {
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findUserById(Long id) {
        LOGGER.info("Searching the user by Id - ClientService");
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.WA0003.getMessage(), ErrorCode.WA0003.getCode()));
    }
    public void updateVehicleAttributes(Vehicle vehicleUpdated, Long vehicleId, Long userId) {
        Optional<User> optionalUser = validateUserOptional(userId);

        Optional<Vehicle> optionalVehicle = optionalUser.get().getVehicleList()
                .stream().filter(vehicle -> vehicle.getId().equals(vehicleId)).findFirst();

        Vehicle vehicle = new Vehicle();
        BeanUtils.copyProperties(vehicleUpdated, vehicle);
        vehicle.setId(optionalVehicle.get().getId());
        vehicle.setCreatedAt(optionalVehicle.get().getCreatedAt());

        optionalUser.get().getVehicleList().remove(optionalVehicle.get());
        optionalUser.get().getVehicleList().add(vehicle);
    }

    private Optional<User> validateUserOptional(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(ErrorCode.WA0003.getMessage(), ErrorCode.WA0003.getCode());
        }
        return optionalUser;
    }

    public void deleteVehicleRegistered(Long userId, Long vehicleId) {
        Optional<User>  optionalUser = validateUserOptional(userId);

        Optional<Vehicle> optionalVehicle = optionalUser.get().getVehicleList()
                .stream().filter(vehicle -> vehicle.getId().equals(vehicleId)).findFirst();

        if (optionalVehicle.isEmpty()) {
            throw new VehicleNotFoundException(ErrorCode.WA0002.getMessage(), ErrorCode.WA0002.getCode());
        }
        optionalUser.get().getVehicleList().remove(optionalVehicle.get());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(
                ErrorCode.WA0003.getMessage(), ErrorCode.WA0003.getCode()));
    }

    public String signUpUser(User userRegisterRequest) {
       boolean userExists = userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent();

       if (userExists) {
           LOGGER.error("The email was already taken - ClientService");
           throw new BadRequestException(ErrorCode.EM0002.getMessage(), ErrorCode.EM0002.getCode());
       }
       BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
       String encodedPassword = bCryptPasswordEncoder.encode(userRegisterRequest.getPassword());

       if (!bCryptPasswordEncoder.matches(userRegisterRequest.getPassword(), encodedPassword)) {
           throw new PasswordNotValidException(ErrorCode.ON0003.getMessage(), ErrorCode.ON0003.getCode());
        }

       userRegisterRequest.setPassword(encodedPassword);
       userRepository.save(userRegisterRequest);

       String accountConfirmationToken = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                accountConfirmationToken,
                userRegisterRequest,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(20)
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        emailService.send(userRegisterRequest.getFullName(), userRegisterRequest.getEmail());

       return accountConfirmationToken;
    }
    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }
}
