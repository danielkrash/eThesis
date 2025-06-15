// Custom JavaScript for University Thesis Portal

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing components...');
    
    // Check if Bootstrap is available
    if (typeof bootstrap !== 'undefined') {
        console.log('Bootstrap is available, initializing dropdowns...');
        
        // Initialize Bootstrap dropdowns explicitly
        var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
        console.log('Found ' + dropdownElementList.length + ' dropdown toggles');
        
        var dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
            console.log('Initializing dropdown for:', dropdownToggleEl);
            return new bootstrap.Dropdown(dropdownToggleEl);
        });
        
        console.log('Dropdowns initialized:', dropdownList.length);
    } else {
        console.error('Bootstrap is not available!');
    }
    
    // Initialize tooltips
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    // Initialize popovers
    if (typeof bootstrap !== 'undefined' && bootstrap.Popover) {
        var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }

    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert-dismissible');
        alerts.forEach(function(alert) {
            const closeButton = alert.querySelector('.btn-close');
            if (closeButton) {
                closeButton.click();
            }
        });
    }, 5000);

    // File upload drag and drop functionality
    const fileUploadAreas = document.querySelectorAll('.file-upload-area');
    fileUploadAreas.forEach(function(area) {
        const fileInput = area.querySelector('input[type="file"]');
        
        if (fileInput) {
            // Click to browse
            area.addEventListener('click', function() {
                fileInput.click();
            });

            // Drag and drop
            area.addEventListener('dragover', function(e) {
                e.preventDefault();
                area.classList.add('dragover');
            });

            area.addEventListener('dragleave', function() {
                area.classList.remove('dragover');
            });

            area.addEventListener('drop', function(e) {
                e.preventDefault();
                area.classList.remove('dragover');
                
                const files = e.dataTransfer.files;
                if (files.length > 0) {
                    fileInput.files = files;
                    handleFileSelection(fileInput, area);
                }
            });

            // Handle file selection
            fileInput.addEventListener('change', function() {
                handleFileSelection(fileInput, area);
            });
        }
    });

    // Form validation enhancement
    const forms = document.querySelectorAll('.needs-validation');
    forms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // HTMX configuration
    if (typeof htmx !== 'undefined') {
        // Show loading spinner on HTMX requests
        document.body.addEventListener('htmx:beforeRequest', function(evt) {
            const target = evt.target;
            if (target.classList.contains('htmx-loading')) {
                target.innerHTML = '<div class="d-flex justify-content-center"><div class="spinner-border spinner-border-sm" role="status"></div></div>';
            }
        });

        // Handle HTMX errors
        document.body.addEventListener('htmx:responseError', function(evt) {
            showAlert('An error occurred while processing your request. Please try again.', 'danger');
        });
    }

    // Search functionality
    const searchInputs = document.querySelectorAll('.search-input');
    searchInputs.forEach(function(input) {
        let searchTimeout;
        input.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(function() {
                performSearch(input.value, input.dataset.target);
            }, 300);
        });
    });

    // Confirmation dialogs
    const confirmButtons = document.querySelectorAll('[data-confirm]');
    confirmButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            const message = button.dataset.confirm;
            if (!confirm(message)) {
                e.preventDefault();
                return false;
            }
        });
    });
});

// Utility functions
function handleFileSelection(fileInput, area) {
    const files = fileInput.files;
    const fileInfo = area.querySelector('.file-info');
    
    if (files.length > 0) {
        const file = files[0];
        const fileName = file.name;
        const fileSize = formatFileSize(file.size);
        
        if (fileInfo) {
            fileInfo.innerHTML = `
                <div class="alert alert-success mb-0">
                    <i class="bi bi-file-earmark-check me-2"></i>
                    <strong>${fileName}</strong> (${fileSize})
                </div>
            `;
        }
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function showAlert(message, type = 'info') {
    const alertContainer = document.querySelector('.alert-container') || document.body;
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        <i class="bi bi-info-circle me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    alertContainer.insertBefore(alert, alertContainer.firstChild);
    
    // Auto-hide after 5 seconds
    setTimeout(function() {
        alert.remove();
    }, 5000);
}

function performSearch(query, target) {
    if (!query || query.length < 2) return;
    
    // This would typically make an AJAX request to search
    // For now, we'll just filter visible elements
    const targetElement = document.querySelector(target);
    if (targetElement) {
        const items = targetElement.querySelectorAll('.searchable-item');
        items.forEach(function(item) {
            const text = item.textContent.toLowerCase();
            if (text.includes(query.toLowerCase())) {
                item.style.display = '';
            } else {
                item.style.display = 'none';
            }
        });
    }
}

function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(function() {
            showAlert('Copied to clipboard!', 'success');
        });
    } else {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showAlert('Copied to clipboard!', 'success');
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Status update functions
function updateProposalStatus(proposalId, newStatus) {
    if (!confirm(`Are you sure you want to change the status to ${newStatus}?`)) {
        return;
    }
    
    fetch(`/api/thesis-proposals/${proposalId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: newStatus })
    })
    .then(response => {
        if (response.ok) {
            showAlert('Status updated successfully!', 'success');
            // Reload the page or update the UI
            location.reload();
        } else {
            throw new Error('Failed to update status');
        }
    })
    .catch(error => {
        showAlert('Error updating status: ' + error.message, 'danger');
    });
}

// Theme toggle (if you want to add dark mode later)
function toggleTheme() {
    const body = document.body;
    const isDark = body.classList.contains('dark-theme');
    
    if (isDark) {
        body.classList.remove('dark-theme');
        localStorage.setItem('theme', 'light');
    } else {
        body.classList.add('dark-theme');
        localStorage.setItem('theme', 'dark');
    }
}

// Initialize theme from localStorage
function initializeTheme() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-theme');
    }
}

// Call on page load
initializeTheme();
