// Constants
const API_URL = 'http://localhost:8080';
let currentUserEmail = null;

// DOM Elements
const views = {
    auth: document.getElementById('auth-view'),
    app: document.getElementById('main-app-view')
};
const forms = {
    login: document.getElementById('login-form'),
    register: document.getElementById('register-form'),
    addBook: document.getElementById('add-book-form')
};

// UI Helpers
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast show ${type}`;
    setTimeout(() => {
        toast.className = 'toast hidden';
    }, 3000);
}

// Global View Switcher (Auth vs App)
function switchMainView(viewName) {
    Object.values(views).forEach(v => {
        v.classList.remove('active');
        v.classList.add('hidden');
    });
    if(viewName === 'auth') {
        views.auth.classList.add('active');
        views.auth.classList.remove('hidden');
    } else if(viewName === 'app') {
        views.app.classList.add('active');
        views.app.classList.remove('hidden');
    }
}

// Side-Nav UI Router
function switchPage(pageId, navButton) {
    // Hidden all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
        page.classList.add('hidden');
    });
    // Show active page
    const actPage = document.getElementById(pageId);
    actPage.classList.remove('hidden');
    actPage.classList.add('active');

    // Update nav active states
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    if(navButton) {
        navButton.classList.add('active');
    }

    // Refresh data if books directory is opened
    if(pageId === 'page-books' || pageId === 'page-overview' || pageId === 'page-admin') {
        if(pageId === 'page-books') fetchBooks();
        if(pageId === 'page-overview') {
            const isAdmin = currentUserEmail && (currentUserEmail === 'admin@example.com' || currentUserEmail.includes('admin'));
            if(isAdmin) fetchOverdueStudents();
            else fetchMyRenewals();
        }
        if(pageId === 'page-admin') fetchStudents();
    }
}



// Authentication API Calls
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            currentUserEmail = email;
            showToast('Login successful!');
            document.getElementById('user-display').textContent = email;
            
            // Show Admin sections if admin
            if(email === 'admin@example.com' || email.includes('admin')) {
                document.getElementById('nav-admin').classList.remove('hidden');
                document.getElementById('overdue-section').classList.remove('hidden');
                document.getElementById('student-borrow-section').classList.add('hidden');
            } else {
                document.getElementById('nav-admin').classList.add('hidden');
                document.getElementById('overdue-section').classList.add('hidden');
                document.getElementById('student-borrow-section').classList.remove('hidden');
            }

            switchMainView('app');
            // Default to overview page
            switchPage('page-overview', document.querySelectorAll('.nav-btn')[0]);
        } else {
            showToast('Invalid credentials', 'error');
        }
    } catch (error) {
        showToast('Connection error', 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;

    try {
        const response = await fetch(`${API_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role: 'USER' })
        });

        if (response.ok) {
            showToast('Registration successful! Please login.');
            toggleAuthMode(null, 'login');
        } else {
            const errorMsg = await response.text();
            showToast(errorMsg || 'Registration failed', 'error');
        }
    } catch (error) {
        showToast('Connection error', 'error');
    }
}

function toggleAuthMode(e, mode) {
    if(e) e.preventDefault();
    if(mode === 'register') {
        forms.login.classList.add('hidden');
        forms.register.classList.remove('hidden');
        document.querySelector('.auth-header p').textContent = 'Create your account to get started.';
    } else {
        forms.register.classList.add('hidden');
        forms.login.classList.remove('hidden');
        document.querySelector('.auth-header p').textContent = 'Welcome back. Please login to your account.';
    }
}



function logout() {
    currentUserEmail = null;
    showToast('Logged out successfully');
    switchMainView('auth');
    forms.login.reset();
}

// Data API Calls
async function fetchBooks() {
    const grid = document.getElementById('books-grid');
    if(!grid.classList.contains('active') && grid.closest('.page.active') == null && document.getElementById('page-overview').classList.contains('hidden')) {
       // Do not fetch if neither books directory nor overview is open.
       return; 
    }
    
    if(grid) grid.innerHTML = '<div class="loading">Loading books...</div>';

    try {
        const response = await fetch(`${API_URL}/books`, {
            credentials: 'include'
        });
        if (response.ok) {
            const books = await response.json();
            updateStats(books);
            if(grid) renderBooks(books);
        } else {
            showToast('Failed to fetch books', 'error');
        }
    } catch (error) {
        showToast('Connection error', 'error');
    }
}

function updateStats(books) {
    let totalQuantity = 0;
    let availableCount = 0;
    
    books.forEach(book => {
        totalQuantity += (book.quantity || 1);
        availableCount += (book.availableCopies || 0);
    });

    document.getElementById('stat-total-books').textContent = totalQuantity;
    document.getElementById('stat-available-books').textContent = availableCount;
    document.getElementById('stat-borrowed-books').textContent = totalQuantity - availableCount;
}

function renderBooks(books) {
    const grid = document.getElementById('books-grid');
    if (books.length === 0) {
        grid.innerHTML = '<div class="loading">No books available in the library yet.</div>';
        return;
    }
    grid.innerHTML = books.map(book => {
        const hasCopies = book.availableCopies > 0;
        const statusClass = hasCopies ? 'status-available' : 'status-borrowed';
        const statusText = hasCopies ? `${book.availableCopies} / ${book.quantity} Available` : 'Out of Stock';
        
        let actionBtn = '';
        const isAdmin = currentUserEmail && currentUserEmail.includes('admin');
        if (isAdmin) {
            actionBtn = `<button class="btn btn-outline disabled" style="padding: 0.5rem; width: 100%; cursor: default;" disabled>Admin Account</button>`;
        } else if (hasCopies) {
            actionBtn = `<button class="btn btn-primary" style="padding: 0.5rem" onclick="borrowBook(${book.id})">Borrow</button>`;
        } else {
            actionBtn = `<button class="btn btn-outline disabled" style="padding: 0.5rem" disabled>No Copies Left</button>`;
        }

        const deleteBtn = (currentUserEmail && currentUserEmail.includes('admin')) 
            ? `<button class="btn btn-outline" style="border-color: var(--danger); color: var(--danger); padding: 0.5rem; width:100%" onclick="deleteBook(${book.id})">Delete (Admin)</button>` 
            : '';

        return `
            <div class="book-card">
                <div class="book-title">${book.title}</div>
                <div class="book-author">by ${book.author}</div>
                <div class="book-status ${statusClass}">${statusText}</div>
                <div class="book-actions">
                    ${actionBtn}
                </div>
                ${deleteBtn}
            </div>
        `;
    }).join('');
}

async function fetchOverdueStudents() {
    const overdueList = document.getElementById('overdue-list');
    if(!overdueList) return;

    try {
        const response = await fetch(`${API_URL}/renewals/overdue`, {
            credentials: 'include'
        });
        if (response.ok) {
            const students = await response.json();
            if(students.length === 0) {
                overdueList.innerHTML = '<p class="text-success">No overdue students found.</p>';
            } else {
                overdueList.innerHTML = students.map(name => `
                    <div class="overdue-item">
                        <span class="student-name">⚠️ ${name}</span>
                        <span class="status-borrowed">Time Exceeded</span>
                    </div>
                `).join('');
            }
        }
    } catch (error) {
        console.error('Failed to fetch overdue students', error);
    }
}

async function fetchMyRenewals() {
    const list = document.getElementById('my-renewals-list');
    if(!list) return;

    try {
        const response = await fetch(`${API_URL}/renewals/my`, {
            credentials: 'include'
        });
        if (response.ok) {
            const renewals = await response.json();
            if(renewals.length === 0) {
                list.innerHTML = '<p class="text-muted">You have no active borrowed books.</p>';
            } else {
                list.innerHTML = renewals.map(ren => `
                    <div class="overdue-item" style="padding: 1.5rem; justify-content: space-between;">
                        <div style="display:flex; flex-direction:column; gap:0.25rem;">
                            <span class="student-name" style="font-size:1.1rem; font-weight:600;">📚 ${ren.borrow.book.title}</span>
                            <span class="${ren.daysRemaining <= 1 ? 'text-danger' : 'text-success'}" style="font-size:0.9rem;">
                                ${ren.daysRemaining} days remaining
                            </span>
                        </div>
                    </div>
                `).join('');
            }
        }
    } catch (error) {
        console.error('Failed to fetch my renewals', error);
    }
}

let studentSearchTimeout;
function handleStudentSearch(query) {
    clearTimeout(studentSearchTimeout);
    studentSearchTimeout = setTimeout(() => {
        fetchStudents(query);
    }, 300);
}

async function fetchStudents(query = '') {
    const list = document.getElementById('student-list');
    if(!list) return;

    try {
        const url = query ? `${API_URL}/api/admin/students?search=${encodeURIComponent(query)}` : `${API_URL}/api/admin/students`;
        const response = await fetch(url, { credentials: 'include' });
        if (response.ok) {
            const students = await response.json();
            renderStudents(students);
        }
    } catch (error) {
        console.error('Failed to fetch students', error);
    }
}

async function renderStudents(students) {
    const list = document.getElementById('student-list');
    if(students.length === 0) {
        list.innerHTML = '<p class="text-muted">No students found.</p>';
        return;
    }

    let html = '';
    for(const student of students) {
        html += `
            <div class="overdue-item" style="flex-direction: column; align-items: flex-start; gap: 1rem; margin-bottom: 1rem; padding: 1.5rem;">
                <div style="display:flex; justify-content:space-between; width:100%; align-items:center;">
                    <div>
                        <div class="student-name" style="font-size: 1.1rem; font-weight: 600;">${student.name}</div>
                        <div class="text-muted" style="font-size:0.85rem;">${student.email}</div>
                    </div>
                    <button class="btn btn-outline" style="width:auto; padding: 0.5rem 1rem;" onclick="fetchStudentBorrows(${student.id}, '${student.email}')">View Books</button>
                </div>
                <div id="borrows-${student.id}" class="hidden" style="width:100%; border-top: 1px solid var(--panel-border); padding-top: 1rem; margin-top: 0.5rem;">
                    <div class="loading">Loading books...</div>
                </div>
            </div>
        `;
    }
    list.innerHTML = html;
}

async function fetchStudentBorrows(userId, email) {
    const container = document.getElementById(`borrows-${userId}`);
    container.classList.toggle('hidden');
    if(container.classList.contains('hidden')) return;

    try {
        // We use the existing /renewals/my but we'll need an admin version in the backend
        // Actually, I'll add /api/renewals/admin/user/{email} to the backend
        const response = await fetch(`${API_URL}/renewals/admin/user/${email}`, { credentials: 'include' });
        if(response.ok) {
            const renewals = await response.json();
            if(renewals.length === 0) {
                container.innerHTML = '<p class="text-muted">No active borrows.</p>';
            } else {
                container.innerHTML = renewals.map(ren => `
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 0.5rem; background: rgba(0,0,0,0.1); padding: 0.8rem; border-radius: 8px;">
                        <span style="font-size:0.9rem;">📚 ${ren.borrow.book.title} (${ren.daysRemaining}d)</span>
                        <div style="display:flex; gap:0.5rem;">
                            <button class="btn btn-outline" style="width:auto; padding: 0.3rem 0.6rem; font-size:0.75rem;" onclick="manualRenew(${ren.borrow.id}, '${email}', ${userId})">Renew</button>
                            <button class="btn btn-primary" style="width:auto; padding: 0.3rem 0.6rem; font-size:0.75rem;" onclick="returnBook(${ren.borrow.id}, true, ${userId}, '${email}')">Return</button>
                        </div>
                    </div>
                `).join('');
            }
        }
    } catch (err) {
        console.error(err);
    }
}

async function manualRenew(borrowId, email, userId) {
    try {
        const response = await fetch(`${API_URL}/renewals/admin/renew/${borrowId}`, {
            method: 'POST',
            credentials: 'include'
        });
        if(response.ok) {
            showToast('Book renewed successfully!');
            fetchStudentBorrows(userId, email); // Refresh the list
        } else {
            showToast('Failed to renew book', 'error');
        }
    } catch (err) {
        console.error(err);
    }
}

async function handleAddBook(e) {
    e.preventDefault();
    const title = document.getElementById('book-title').value;
    const author = document.getElementById('book-author').value;
    const isbn = document.getElementById('book-isbn').value;

    try {
        const response = await fetch(`${API_URL}/books`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, author, isbn, categoryId: 1 }) 
        });

        if (response.ok) {
            showToast('Book added successfully');
            forms.addBook.reset();
            fetchBooks();
        } else {
            showToast('Failed to add book', 'error');
        }
    } catch (error) {
        showToast('Connection error', 'error');
    }
}

async function borrowBook(bookId) {
    try {
        const response = await fetch(`${API_URL}/borrow`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookId })
        });

        if (response.ok) {
            showToast('Book borrowed successfully!');
            fetchBooks();
        } else {
            const err = await response.text();
            showToast(err || 'Failed to borrow book', 'error');
        }
    } catch (error) {
        showToast('Connection error', 'error');
    }
}

// Standardized Return function for both Students and Admins
async function returnBook(borrowId, isAdminAction = false, userId = null, email = null) {
    if(!confirm('Are you sure you want to return this book?')) return;

    try {
        const response = await fetch(`${API_URL}/borrow/return/${borrowId}`, {
            method: 'PUT',
            credentials: 'include'
        });

        if (response.ok) {
            showToast('Book returned successfully!');
            fetchBooks();
            if (isAdminAction && userId && email) {
                fetchStudentBorrows(userId, email); // Refresh admin's view of student
            } else {
                fetchMyRenewals(); // Refresh student's personal borrows
                if (typeof fetchOverdueStudents === 'function') fetchOverdueStudents();
            }
        } else {
            const err = await response.text();
            showToast(err || 'Failed to return book', 'error');
        }
    } catch (error) {
        console.error('Error returning book:', error);
        showToast('Error returning book', 'error');
    }
}

async function deleteBook(id) {
    if(!confirm('Are you sure you want to delete this book?')) return;
    try {
        const response = await fetch(`${API_URL}/books/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            showToast('Book deleted');
            fetchBooks();
        } else {
            const err = await response.text();
            showToast(err || 'Failed to delete', 'error');
        }
    } catch (error) {
        showToast('Connection error', 'error');
    }
}
