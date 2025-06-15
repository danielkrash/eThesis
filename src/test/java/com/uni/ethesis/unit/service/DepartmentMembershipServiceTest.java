package com.uni.ethesis.unit.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uni.ethesis.data.dto.DepartmentDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.entities.UserInDepartment;
import com.uni.ethesis.data.repo.DepartmentAppointmentRepository;
import com.uni.ethesis.data.repo.DepartmentRepository;
import com.uni.ethesis.data.repo.UserInDepartmentRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.DepartmentNotFoundException;
import com.uni.ethesis.exceptions.UserNotFoundException;
import com.uni.ethesis.service.impl.DepartmentServiceImpl;
import com.uni.ethesis.utils.mappers.DepartmentAppointmentMapper;
import com.uni.ethesis.utils.mappers.DepartmentMapper;
import com.uni.ethesis.utils.mappers.UserMapper;

@ExtendWith(MockitoExtension.class)
class DepartmentMembershipServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private DepartmentAppointmentRepository departmentAppointmentRepository;

    @Mock
    private DepartmentAppointmentMapper departmentAppointmentMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserInDepartmentRepository userInDepartmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private UUID departmentId;
    private UUID userId;
    private Department department;
    private User user;
    private DepartmentDto departmentDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        department = Department.builder()
                .id(departmentId)
                .name("Computer Science")
                .description("Computer Science Department")
                .build();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        departmentDto = DepartmentDto.builder()
                .id(departmentId)
                .name("Computer Science")
                .description("Computer Science Department")
                .build();

        userDto = new UserDto();
        userDto.setId(userId);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setEmail("test@example.com");
    }

    @Test
    void addUserToDepartment_Success() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userInDepartmentRepository.existsByDepartmentIdAndUserId(departmentId, userId)).thenReturn(false);
        when(departmentMapper.departmentToDepartmentDto(department)).thenReturn(departmentDto);

        // Act
        DepartmentDto result = departmentService.addUserToDepartment(departmentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(departmentId, result.getId());
        verify(userInDepartmentRepository).save(any(UserInDepartment.class));
        verify(departmentMapper).departmentToDepartmentDto(department);
    }

    @Test
    void addUserToDepartment_UserAlreadyMember() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userInDepartmentRepository.existsByDepartmentIdAndUserId(departmentId, userId)).thenReturn(true);
        when(departmentMapper.departmentToDepartmentDto(department)).thenReturn(departmentDto);

        // Act
        DepartmentDto result = departmentService.addUserToDepartment(departmentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(departmentId, result.getId());
        verify(userInDepartmentRepository, never()).save(any(UserInDepartment.class));
        verify(departmentMapper).departmentToDepartmentDto(department);
    }

    @Test
    void addUserToDepartment_DepartmentNotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DepartmentNotFoundException.class, 
                () -> departmentService.addUserToDepartment(departmentId, userId));
    }

    @Test
    void addUserToDepartment_UserNotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, 
                () -> departmentService.addUserToDepartment(departmentId, userId));
    }

    @Test
    void removeUserFromDepartment_Success() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(departmentMapper.departmentToDepartmentDto(department)).thenReturn(departmentDto);

        // Act
        DepartmentDto result = departmentService.removeUserFromDepartment(departmentId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(departmentId, result.getId());
        verify(userInDepartmentRepository).deleteByDepartmentIdAndUserId(departmentId, userId);
        verify(departmentMapper).departmentToDepartmentDto(department);
    }

    @Test
    void removeUserFromDepartment_DepartmentNotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DepartmentNotFoundException.class, 
                () -> departmentService.removeUserFromDepartment(departmentId, userId));
    }

    @Test
    void removeUserFromDepartment_UserNotFound() {
        // Arrange
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, 
                () -> departmentService.removeUserFromDepartment(departmentId, userId));
    }

    @Test
    void isUserMemberOfDepartment_True() {
        // Arrange
        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userInDepartmentRepository.existsByDepartmentIdAndUserId(departmentId, userId)).thenReturn(true);

        // Act
        boolean result = departmentService.isUserMemberOfDepartment(departmentId, userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isUserMemberOfDepartment_False() {
        // Arrange
        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userInDepartmentRepository.existsByDepartmentIdAndUserId(departmentId, userId)).thenReturn(false);

        // Act
        boolean result = departmentService.isUserMemberOfDepartment(departmentId, userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isUserMemberOfDepartment_DepartmentNotFound() {
        // Arrange
        when(departmentRepository.existsById(departmentId)).thenReturn(false);

        // Act & Assert
        assertThrows(DepartmentNotFoundException.class, 
                () -> departmentService.isUserMemberOfDepartment(departmentId, userId));
    }

    @Test
    void isUserMemberOfDepartment_UserNotFound() {
        // Arrange
        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, 
                () -> departmentService.isUserMemberOfDepartment(departmentId, userId));
    }

    @Test
    void getDepartmentMembers_Success() {
        // Arrange
        UUID userId2 = UUID.randomUUID();
        User user2 = User.builder()
                .id(userId2)
                .email("test2@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        UserDto userDto2 = new UserDto();
        userDto2.setId(userId2);
        userDto2.setFirstName("Jane");
        userDto2.setLastName("Smith");
        userDto2.setEmail("test2@example.com");

        UserInDepartment uid1 = UserInDepartment.builder()
                .user(user)
                .department(department)
                .build();

        UserInDepartment uid2 = UserInDepartment.builder()
                .user(user2)
                .department(department)
                .build();

        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(userInDepartmentRepository.findByDepartmentId(departmentId))
                .thenReturn(Arrays.asList(uid1, uid2));
        when(userMapper.userToUserDto(user)).thenReturn(userDto);
        when(userMapper.userToUserDto(user2)).thenReturn(userDto2);

        // Act
        List<UserDto> result = departmentService.getDepartmentMembers(departmentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(userId)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(userId2)));
    }

    @Test
    void getDepartmentMembers_DepartmentNotFound() {
        // Arrange
        when(departmentRepository.existsById(departmentId)).thenReturn(false);

        // Act & Assert
        assertThrows(DepartmentNotFoundException.class, 
                () -> departmentService.getDepartmentMembers(departmentId));
    }
}
