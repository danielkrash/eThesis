package com.uni.ethesis.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uni.ethesis.data.dto.DepartmentAppointmentDto;
import com.uni.ethesis.data.dto.DepartmentDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.data.entities.DepartmentAppointment;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.entities.UserInDepartment;
import com.uni.ethesis.data.repo.DepartmentAppointmentRepository;
import com.uni.ethesis.data.repo.DepartmentRepository;
import com.uni.ethesis.data.repo.UserInDepartmentRepository;
import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.exceptions.AppointmentConflictException;
import com.uni.ethesis.exceptions.DepartmentAppointmentNotFoundException;
import com.uni.ethesis.exceptions.DepartmentNotFoundException;
import com.uni.ethesis.exceptions.UserNotFoundException;
import com.uni.ethesis.service.DepartmentService;
import com.uni.ethesis.utils.mappers.DepartmentAppointmentMapper;
import com.uni.ethesis.utils.mappers.DepartmentMapper;
import com.uni.ethesis.utils.mappers.UserMapper;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final DepartmentAppointmentRepository departmentAppointmentRepository;
    private final DepartmentAppointmentMapper departmentAppointmentMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserInDepartmentRepository userInDepartmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper,
            DepartmentAppointmentRepository departmentAppointmentRepository,
            DepartmentAppointmentMapper departmentAppointmentMapper, UserRepository userRepository, UserMapper userMapper,
            UserInDepartmentRepository userInDepartmentRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
        this.departmentAppointmentRepository = departmentAppointmentRepository;
        this.departmentAppointmentMapper = departmentAppointmentMapper;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userInDepartmentRepository = userInDepartmentRepository;
    }

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        Department department = departmentMapper.departmentDtoToDepartment(departmentDto);
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.departmentToDepartmentDto(savedDepartment);
    }

    @Override
    public DepartmentDto getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + id));
        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    public DepartmentDto getDepartmentByName(String name) {
        Department department = departmentRepository.findByNameIgnoreCase(name);
        if (department == null) {
            throw new DepartmentNotFoundException("Department not found with name: " + name);
        }
        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(UUID id, DepartmentDto departmentDto) {
        return departmentRepository.findById(id)
                .map(department -> {
                    departmentMapper.updateDepartmentFromDto(departmentDto, department);
                    Department updatedDepartment = departmentRepository.save(department);
                    return departmentMapper.departmentToDepartmentDto(updatedDepartment);
                })
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + id));
        departmentRepository.delete(department);
    }

    @Override
    public List<DepartmentDto> getDepartmentsByUserId(UUID userId) {
        return departmentRepository.findDepartmentsByUserId(userId).stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentDto> getDepartmentsByHeadId(UUID headId) {
        return departmentRepository.findDepartmentsByHeadId(headId).stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDto getDepartmentByCurrentHeadId(UUID headId) {
        Department department = departmentRepository.findDepartmentByCurrentHeadId(headId, OffsetDateTime.now());
        if (department == null) {
            throw new DepartmentNotFoundException("Department not found for current head with id: " + headId);
        }
        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    @Transactional
    public DepartmentDto appointDepartmentHead(UUID departmentId, UUID userId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Validate that the department exists
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + departmentId));

        // Validate that the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check if there's already a current head for this department
        if (departmentAppointmentRepository.hasCurrentHead(departmentId, OffsetDateTime.now())) {
            throw new AppointmentConflictException("Department already has a current head. End the current appointment first.");
        }

        // Check for overlapping appointments if endDate is provided
        if (endDate != null) {
            List<DepartmentAppointment> overlapping = departmentAppointmentRepository
                    .findOverlappingAppointments(departmentId, startDate, endDate);
            if (!overlapping.isEmpty()) {
                throw new AppointmentConflictException("Appointment period overlaps with existing appointments.");
            }
        }

        // Create the appointment
        DepartmentAppointment appointment = DepartmentAppointment.builder()
                .department(department)
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        departmentAppointmentRepository.save(appointment);
        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    @Transactional
    public DepartmentDto endCurrentHeadAppointment(UUID departmentId, OffsetDateTime endDate) {
        // Validate that the department exists
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + departmentId));

        // Find current appointment
        DepartmentAppointment currentAppointment = departmentAppointmentRepository
                .findCurrentAppointmentByDepartmentId(departmentId, OffsetDateTime.now())
                .orElseThrow(() -> new DepartmentAppointmentNotFoundException("No current head appointment found for department: " + departmentId));

        // Update the end date
        currentAppointment.setEndDate(endDate);
        departmentAppointmentRepository.save(currentAppointment);

        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentDepartmentHead(UUID departmentId) {
        // Validate that the department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException("Department not found with id: " + departmentId);
        }

        DepartmentAppointment currentAppointment = departmentAppointmentRepository
                .findCurrentAppointmentByDepartmentId(departmentId, OffsetDateTime.now())
                .orElse(null);

        if (currentAppointment == null) {
            return null; // No current head
        }

        return userMapper.userToUserDto(currentAppointment.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentAppointmentDto> getDepartmentAppointmentHistory(UUID departmentId) {
        // Validate that the department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException("Department not found with id: " + departmentId);
        }

        List<DepartmentAppointment> appointments = departmentAppointmentRepository
                .findByDepartmentIdOrderByStartDateDesc(departmentId);

        List<DepartmentAppointmentDto> appointmentDtos = departmentAppointmentMapper
                .departmentAppointmentsToDto(appointments);

        // Set the isCurrent flag for each appointment
        OffsetDateTime now = OffsetDateTime.now();
        appointmentDtos.forEach(dto -> {
            boolean isCurrent = dto.getStartDate() != null && 
                               dto.getStartDate().isBefore(now.toLocalDateTime()) && 
                               (dto.getEndDate() == null || dto.getEndDate().isAfter(now.toLocalDateTime()));
            dto.setCurrent(isCurrent);
        });

        return appointmentDtos;
    }

    // Regular department membership methods (UserInDepartment)
    @Override
    @Transactional
    public DepartmentDto addUserToDepartment(UUID departmentId, UUID userId) {
        // Validate that the department exists
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + departmentId));

        // Validate that the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check if the user is already a member of the department
        if (userInDepartmentRepository.existsByDepartmentIdAndUserId(departmentId, userId)) {
            // User is already a member, just return the department
            return departmentMapper.departmentToDepartmentDto(department);
        }

        // Create new UserInDepartment relationship
        UserInDepartment userInDepartment = UserInDepartment.builder()
                .user(user)
                .department(department)
                .build();

        userInDepartmentRepository.save(userInDepartment);

        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    @Transactional
    public DepartmentDto removeUserFromDepartment(UUID departmentId, UUID userId) {
        // Validate that the department exists
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + departmentId));

        // Validate that the user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        // Remove the user from the department
        userInDepartmentRepository.deleteByDepartmentIdAndUserId(departmentId, userId);

        return departmentMapper.departmentToDepartmentDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMemberOfDepartment(UUID departmentId, UUID userId) {
        // Validate that the department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException("Department not found with id: " + departmentId);
        }

        // Validate that the user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return userInDepartmentRepository.existsByDepartmentIdAndUserId(departmentId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getDepartmentMembers(UUID departmentId) {
        // Validate that the department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException("Department not found with id: " + departmentId);
        }

        List<UserInDepartment> userInDepartments = userInDepartmentRepository.findByDepartmentId(departmentId);

        return userInDepartments.stream()
                .map(uid -> userMapper.userToUserDto(uid.getUser()))
                .collect(Collectors.toList());
    }
}
