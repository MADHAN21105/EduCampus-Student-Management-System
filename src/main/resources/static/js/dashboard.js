document.addEventListener('DOMContentLoaded', () => {
    console.log('Dashboard script loaded. Checking auth...');
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    console.log('User detected:', username, 'Role:', role);

    const greeting = document.getElementById('userGreeting');
    if (greeting) {
        if (role === 'STUDENT') greeting.textContent = `Welcome back ${username}`;
        else greeting.textContent = `${role.charAt(0) + role.slice(1).toLowerCase()} Portal`;
    }

    // Fetch stats based on role
    try {
        if (role === 'ADMIN') {
            console.log('Initializing Admin Dashboard...');
            fetchStats();
            fetchStudents();
            fetchTeachers();
            fetchSubjects();
            fetchDepartmentAverages();
            fetchSemesterWindows();
        } else if (role === 'TEACHER') {
            console.log('Initializing Teacher Dashboard...');
            updateMarksFilters();
            fetchPendingLeaveRequests();
            fetchTeacherProfile();
            fetchSemesterWindows();

            // Poll for new leave requests every 30 seconds
            setInterval(fetchPendingLeaveRequests, 30000);
        } else if (role === 'STUDENT') {
            console.log('Initializing Student Dashboard...');
            if (typeof showSection === 'function') {
                const profileLink = document.querySelector('a[onclick*="profile-section"]');
                showSection('profile-section', profileLink);
            } else {
                const pSec = document.getElementById('profile-section');
                if (pSec) pSec.style.display = 'block';
            }

            fetchStudentProfile();
            fetchStudentStats();
            if (typeof fetchAiInsights === 'function') fetchAiInsights();
            fetchStudentAttendance();
            fetchSemesterResults();
            fetchStudentLeaveRequests();
        }
    } catch (initError) {
        console.error('Critical Error during dashboard initialization:', initError);
    }

    // Set today's date as default for attendance marking
    const dateInput = document.getElementById('att-date');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.value = today;
        console.log('Set attendance date to:', today);
    }

    // Handle Leave Request Form
    const leaveForm = document.getElementById('leaveRequestForm');
    if (leaveForm) {
        leaveForm.addEventListener('submit', applyForLeave);
    }

    // Handle form submission
    const studentForm = document.getElementById('addStudentForm');
    if (studentForm) {
        studentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('std-name').value;
            const rollNo = document.getElementById('std-roll').value;
            const email = document.getElementById('std-email').value;
            const className = document.getElementById('std-year').value; // Using year input as class/year
            const year = document.getElementById('std-year').value;
            const department = document.getElementById('std-dept').value;

            const phone = document.getElementById('std-phone').value;
            const gender = document.getElementById('std-gender').value;
            const dob = document.getElementById('std-dob').value;
            const bloodGroup = document.getElementById('std-blood').value;
            const fatherName = document.getElementById('std-father').value;
            const motherName = document.getElementById('std-mother').value;
            const address = document.getElementById('std-address').value;

            try {
                // Step 1: Create User Account
                const userResponse = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({
                        username: rollNo,
                        password: name,
                        role: 'STUDENT'
                    })
                });

                if (!userResponse.ok) {
                    const errorData = await userResponse.text();
                    throw new Error(`User account creation failed: ${errorData}`);
                }

                // Step 2: Update Student Profile with detailed info
                const semester = document.getElementById('std-semester').value;
                const response = await fetch('/api/students', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify({
                        name, rollNo, email, phone, studentClass: className, year, department,
                        semester: parseInt(semester),
                        gender, dob, fatherName, motherName, bloodGroup, address
                    })
                });

                if (response.ok) {
                    alert('Student added successfully!');
                    studentForm.reset();
                    if (typeof toggleStudentForm === 'function') toggleStudentForm();
                    fetchStudents();
                    fetchStats();
                } else {
                    const errorData = await response.text();
                    alert(`Student profile update failed: ${errorData}`);
                }
            } catch (error) {
                console.error('Error adding student:', error);
                alert('Error: ' + error.message);
            }
        });
    }

    // Teacher Form Submission
    const teacherForm = document.getElementById('addTeacherForm');
    if (teacherForm) {
        teacherForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('t-name').value;
            const facultyId = document.getElementById('t-faculty-id').value;
            const email = document.getElementById('t-email').value;
            const phone = document.getElementById('t-phone').value;
            const specialization = document.getElementById('t-special').value;
            const department = document.getElementById('t-dept').value;

            // New fields
            const dob = document.getElementById('t-dob').value;
            const gender = document.getElementById('t-gender').value;
            const bloodGroup = document.getElementById('t-blood').value;
            const fatherName = document.getElementById('t-father').value;
            const motherName = document.getElementById('t-mother').value;
            const address = document.getElementById('t-address').value;

            try {
                // Step 1: Create User Account
                const userResponse = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({
                        username: facultyId,
                        password: name,
                        role: 'TEACHER'
                    })
                });

                if (!userResponse.ok) {
                    const errorData = await userResponse.text();
                    throw new Error(`User account creation failed: ${errorData}`);
                }

                // Step 2: Update Teacher Profile with detailed info
                const response = await fetch('/api/teachers', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify({
                        name, facultyId, email, phone, specialization, department,
                        dob, gender, bloodGroup, fatherName, motherName, address
                    })
                });

                if (response.ok) {
                    alert('Teacher added successfully!');
                    teacherForm.reset();
                    if (typeof toggleTeacherForm === 'function') toggleTeacherForm();
                    fetchTeachers();
                    fetchStats();
                } else {
                    const errorData = await response.text();
                    alert(`Teacher profile update failed: ${errorData}`);
                }
            } catch (error) {
                console.error('Error adding teacher:', error);
                alert('Error: ' + error.message);
            }
        });
    }

    // Subject Form Submission
    const subjectForm = document.getElementById('addSubjectForm');
    if (subjectForm) {
        subjectForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('s-name').value;
            const code = document.getElementById('s-code').value;
            const department = document.getElementById('s-dept').value;
            const semester = parseInt(document.getElementById('s-sem').value);
            const credits = parseInt(document.getElementById('s-credits').value);

            try {
                const response = await fetch('/api/subjects', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify({ name, code, department, semester, credits })
                });
                if (response.ok) {
                    alert('Subject added successfully!');
                    subjectForm.reset();
                    if (typeof toggleSubjectForm === 'function') toggleSubjectForm();
                    fetchSubjects();
                    fetchStats();
                }
            } catch (error) { console.error('Error adding subject:', error); }
        });
    }

    // User Account Form Submission
    const userAccountForm = document.getElementById('addUserForm');
    if (userAccountForm) {
        userAccountForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const usernameInput = document.getElementById('u-username');
            const roleInput = document.getElementById('u-role');
            const passwordInput = document.getElementById('u-password');
            const confirmInput = document.getElementById('u-confirm-password');

            if (!usernameInput || !roleInput || !passwordInput || !confirmInput) {
                console.error('User account form inputs not found');
                return;
            }

            const username = usernameInput.value;
            const role = roleInput.value;
            const password = passwordInput.value;
            const confirm = confirmInput.value;

            if (password !== confirm) {
                alert('Passwords do not match!');
                return;
            }

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify({ username, password, role })
                });
                if (response.ok) {
                    alert('User account created successfully!');
                    userAccountForm.reset();
                    if (typeof toggleUserForm === 'function') toggleUserForm();
                    fetchStats();
                } else {
                    const error = await response.text();
                    alert('Failed to create user: ' + error);
                }
            } catch (error) { console.error(error); }
        });
    }

}); // End of DOMContentLoaded

// ==========================================
// Global Utility Functions
// ==========================================

async function fetchStats() {
    const token = localStorage.getItem('token');
    console.log('Fetching Admin Stats...');
    try {
        const response = await fetch('/api/dashboard/admin', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            console.log('Stats loaded:', data);

            // Add null checks to prevent errors
            const totalStudents = document.getElementById('totalStudents');
            const totalTeachers = document.getElementById('totalTeachers');
            const totalSubjects = document.getElementById('totalSubjects');
            const totalClasses = document.getElementById('totalClasses');
            const attendanceOverview = document.getElementById('attendanceOverview');

            if (totalStudents) totalStudents.textContent = data.totalStudents;
            if (totalTeachers) totalTeachers.textContent = data.totalTeachers;
            if (totalSubjects) totalSubjects.textContent = data.totalSubjects;
            if (totalClasses) totalClasses.textContent = data.totalClasses || 0;
            if (attendanceOverview) attendanceOverview.textContent = data.attendanceOverview || '0%';
        } else {
            console.error('Stats fetch failed:', response.status);
            if (response.status === 401) {
                window.location.href = 'login.html';
            }
        }
    } catch (error) {
        console.error('Error fetching stats:', error);
    }
}

async function fetchStudents() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('studentTableBody');
    if (!tableBody) return;

    // Get filter values
    const deptFilter = document.getElementById('filter-std-dept') ? document.getElementById('filter-std-dept').value : '';
    const semFilter = document.getElementById('filter-std-semester') ? document.getElementById('filter-std-semester').value : '';

    try {
        const response = await fetch('/api/students', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            let students = await response.json();

            // Apply filtering
            if (deptFilter) {
                students = students.filter(s => s.department === deptFilter);
            }
            if (semFilter) {
                students = students.filter(s => String(s.semester) === String(semFilter));
            }

            // Update counter
            const counter = document.getElementById('filteredStudentCount');
            if (counter) counter.textContent = students.length;

            tableBody.innerHTML = '';
            if (students.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 3rem; color: #64748b; font-style: italic;">No students found matching these filters.</td></tr>';
                return;
            }

            students.forEach(std => {
                const row = `
                    <tr>
                        <td>
                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(std.name)}&background=f0f2f9&color=1a237e" style="width: 32px; border-radius: 8px;">
                                <span style="font-weight: 500;">${std.name}</span>
                            </div>
                        </td>
                        <td style="color: #64748b; font-family: monospace;">${std.rollNo}</td>
                        <td style="color: #64748b;">${std.department || '-'}</td>
                        <td>
                            <button class="btn btn-outline" style="padding: 0.4rem 1rem; font-size: 0.8rem; border: 1px solid #fee2e2; color: #ef4444; border-radius: 6px; transition: all 0.2s;" 
                                    onmouseover="this.style.background='#fef2f2'" onmouseout="this.style.background='transparent'"
                                    onclick="deleteStudent('${std.rollNo}')">Delete</button>
                        </td>
                    </tr>
                `;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        }
    } catch (error) {
        console.error('Error fetching students:', error);
    }
}

async function deleteStudent(rollNo) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    const token = localStorage.getItem('token');
    try {
        const response = await fetch(`/api/students/${rollNo}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            fetchStudents();
            fetchStats();
        }
    } catch (error) {
        console.error('Error deleting student:', error);
    }
}

function toggleStudentForm() {
    const form = document.getElementById('studentForm');
    if (form) form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

function toggleTeacherForm() {
    const form = document.getElementById('teacherForm');
    if (form) form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

function toggleSubjectForm() {
    const form = document.getElementById('subjectForm');
    if (form) form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

async function fetchTeachers() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('teacherTableBody');
    if (!tableBody) return;

    // Get filter values
    const deptFilter = document.getElementById('filter-teacher-dept') ? document.getElementById('filter-teacher-dept').value : '';
    const specialFilter = document.getElementById('filter-teacher-special') ? document.getElementById('filter-teacher-special').value.toLowerCase() : '';

    try {
        const response = await fetch('/api/teachers', { headers: { 'Authorization': `Bearer ${token}` } });
        if (response.ok) {
            let teachers = await response.json();

            // Apply filtering
            if (deptFilter) {
                teachers = teachers.filter(t => t.department === deptFilter);
            }
            if (specialFilter) {
                teachers = teachers.filter(t => t.specialization && t.specialization.toLowerCase().includes(specialFilter));
            }

            // Update counter
            const counter = document.getElementById('filteredTeacherCount');
            if (counter) counter.textContent = teachers.length;

            tableBody.innerHTML = '';
            if (teachers.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 3rem; color: #64748b; font-style: italic;">No teachers found matching these filters.</td></tr>';
                return;
            }

            teachers.forEach(t => {
                tableBody.insertAdjacentHTML('beforeend', `
                    <tr>
                        <td>
                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(t.name)}&background=f0f9ff&color=0369a1&bold=true" style="width: 32px; border-radius: 8px;">
                                <span style="font-weight: 500;">${t.name}</span>
                            </div>
                        </td>
                        <td style="color: #64748b;">${t.department || '-'}</td>
                        <td style="color: #64748b;">
                            <span style="background: #f1f5f9; padding: 0.2rem 0.6rem; border-radius: 6px; font-size: 0.85rem; font-weight: 500; color: #475569;">
                                ${t.specialization || '-'}
                            </span>
                        </td>
                        <td>
                            <button class="btn btn-outline" 
                                    style="padding: 0.4rem 1rem; font-size: 0.8rem; border: 1px solid #fee2e2; color: #ef4444; border-radius: 6px; transition: all 0.2s;"
                                    onmouseover="this.style.background='#fef2f2'" onmouseout="this.style.background='transparent'"
                                    onclick="deleteTeacher('${t.facultyId}')">Delete</button>
                        </td>
                    </tr>
                `);
            });
        }
    } catch (error) { console.error('Error fetching teachers:', error); }
}

async function deleteTeacher(facultyId) {
    if (!confirm('Delete this teacher?')) return;
    const token = localStorage.getItem('token');
    await fetch(`/api/teachers/${facultyId}`, { method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` } });
    fetchTeachers();
    fetchStats();
}

async function fetchSubjects() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('subjectTableBody');
    if (!tableBody) return;

    // Get filter values
    const deptFilter = document.getElementById('filter-sub-dept') ? document.getElementById('filter-sub-dept').value : '';
    const semFilter = document.getElementById('filter-sub-semester') ? document.getElementById('filter-sub-semester').value : '';

    try {
        const response = await fetch('/api/subjects', { headers: { 'Authorization': `Bearer ${token}` } });
        if (response.ok) {
            let subjects = await response.json();

            // Apply filtering
            if (deptFilter) {
                subjects = subjects.filter(s => s.department === deptFilter);
            }
            if (semFilter) {
                subjects = subjects.filter(s => String(s.semester) === String(semFilter));
            }

            // Update counter
            const counter = document.getElementById('filteredSubjectCount');
            if (counter) counter.textContent = subjects.length;

            tableBody.innerHTML = '';
            if (subjects.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 3rem; color: #64748b; font-style: italic;">No subjects found matching these filters.</td></tr>';
                return;
            }

            subjects.forEach(s => {
                tableBody.insertAdjacentHTML('beforeend', `
                    <tr>
                        <td>
                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                <div style="width: 32px; height: 32px; background: #dbeafe; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #1e40af; font-weight: 700; font-size: 0.8rem;">
                                    ${s.name.charAt(0).toUpperCase()}
                                </div>
                                <span style="font-weight: 500;">${s.name}</span>
                            </div>
                        </td>
                        <td style="color: #64748b; font-family: monospace;">${s.code}</td>
                        <td style="color: #64748b; font-size: 0.9rem;">${s.department || '-'}</td>
                        <td style="color: #64748b;">Sem ${s.semester}</td>
                        <td style="color: #64748b;">${s.credits} Credits</td>
                        <td>
                            <button class="btn btn-outline" 
                                    style="padding: 0.4rem 1rem; font-size: 0.8rem; border: 1px solid #fee2e2; color: #ef4444; border-radius: 6px; transition: all 0.2s;"
                                    onmouseover="this.style.background='#fef2f2'" onmouseout="this.style.background='transparent'"
                                    onclick="deleteSubject(${s.id})">Delete</button>
                        </td>
                    </tr>
                `);
            });
        }
    } catch (error) { console.error('Error fetching subjects:', error); }
}

async function deleteSubject(id) {
    if (!confirm('Delete this subject?')) return;
    const token = localStorage.getItem('token');
    await fetch(`/api/subjects/${id}`, { method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` } });
    fetchSubjects();
    fetchStats();
}

// --- Teacher Dashboard Functions ---



// Obsolete function removed - managed subjects are now handled via fetchAndDisplaySubjects

// Managed Subjects Functions (Creation, Filtering, Listing)
window.allSubjectsList = [];

async function fetchAndDisplaySubjects() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('subjectListBody');
    if (!tableBody) return;

    try {
        const response = await fetch('/api/subjects', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            window.allSubjectsList = await response.json();
            console.log('Total subjects loaded:', window.allSubjectsList.length);
            filterSubjects(); // Render with current filters
        }
    } catch (error) {
        console.error('Error fetching subjects for list:', error);
    }
}

function filterSubjects() {
    const tableBody = document.getElementById('subjectListBody');
    if (!tableBody || !window.allSubjectsList) return;

    const deptFilter = document.getElementById('filterSubjDept').value;
    const semFilter = document.getElementById('filterSubjSem').value;

    console.log(`Filtering subjects - Dept: ${deptFilter}, Sem: ${semFilter}`);

    const filtered = window.allSubjectsList.filter(s => {
        const deptMatch = deptFilter === 'All' || s.department === deptFilter;
        const semMatch = semFilter === 'All' || s.semester === parseInt(semFilter);
        return deptMatch && semMatch;
    });

    tableBody.innerHTML = filtered.length === 0 ?
        '<tr><td colspan="5" style="padding: 3rem; text-align: center; color: #94a3b8; font-style: italic;">No subjects match the selected filters.</td></tr>' :
        filtered.map(s => `
            <tr style="border-bottom: 1px solid #f1f5f9; transition: background 0.2s;" onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background='white'">
                <td style="padding: 1.25rem; font-weight: 600; color: #1e293b;">${s.name}</td>
                <td style="padding: 1.25rem; color: #475569;"><span style="background: #f1f5f9; padding: 0.25rem 0.6rem; border-radius: 6px; font-family: monospace; font-size: 0.9rem;">${s.code}</span></td>
                <td style="padding: 1.25rem; color: #475569;">${s.department}</td>
                <td style="padding: 1.25rem;"><span class="badge" style="background: #e0e7ff; color: #4338ca; padding: 0.3rem 0.75rem; border-radius: 20px; font-size: 0.85rem; font-weight: 600;">Semester ${s.semester}</span></td>
                <td style="padding: 1.25rem; text-align: center;"><strong style="color: #2563eb;">${s.credits}</strong></td>
            </tr>
        `).join('');
}

// Obsolete function removed - student selection now handled via side list

async function updateMarksFilters(isHighlightOnly = false) {
    const token = localStorage.getItem('token');
    const dept = document.getElementById('marksDept')?.value;
    const sem = document.getElementById('marksSemester')?.value;
    const listContainer = document.getElementById('studentListContainer');
    const tablePlaceholder = document.getElementById('marksTablePlaceholder');
    const marksContent = document.getElementById('marksEntryContent');

    if (!listContainer) return;

    // Reset UI and selected student ONLY if NOT highlight-only
    if (!isHighlightOnly) {
        if (tablePlaceholder) tablePlaceholder.style.display = 'block';
        if (marksContent) marksContent.style.display = 'none';
        window.currentMarksRollNo = null;
    }

    if (!dept || !sem) {
        listContainer.innerHTML = '<p style="text-align: center; color: #94a3b8; padding: 2rem;">Select Dept/Sem First</p>';
        return;
    }

    // Use cache if it's just a highlight refresh
    if (isHighlightOnly && window.lastFetchedMarksStudents) {
        renderStudentList(window.lastFetchedMarksStudents);
        return;
    }

    listContainer.innerHTML = '<p style="text-align: center; color: #94a3b8; padding: 1.5rem;"><i class="fas fa-spinner fa-spin"></i> Loading Students...</p>';

    try {
        const response = await fetch('/api/students', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const students = await response.json();
            const filtered = students.filter(s => s.department === dept && (!s.semester || s.semester === parseInt(sem)));

            window.lastFetchedMarksStudents = filtered;
            renderStudentList(filtered);
        }
    } catch (error) {
        console.error('Error updating marks filters:', error);
        listContainer.innerHTML = '<p style="text-align: center; color: #ef4444; padding: 1.5rem;">Error loading students.</p>';
    }
}

function renderStudentList(students) {
    const listContainer = document.getElementById('studentListContainer');
    if (!listContainer) return;

    if (students.length === 0) {
        listContainer.innerHTML = '<p style="text-align: center; color: #94a3b8; padding: 1.5rem;">No students found.</p>';
        return;
    }

    listContainer.innerHTML = students.map(std => {
        const isActive = std.rollNo === window.currentMarksRollNo;
        const style = isActive ?
            'background: #f0f9ff; color: #0369a1; font-weight: 700; border-right: 4px solid #0369a1;' :
            'background: transparent; color: #475569; border-right: 4px solid transparent;';

        return `
            <button onclick="loadStudentSubjectsForMarks('${std.rollNo}', '${std.name.replace(/'/g, "\\'")}')" 
                    class="student-list-btn"
                    style="text-align: left; padding: 0.8rem 1rem; cursor: pointer; border: none; border-bottom: 1px solid #f8fafc; transition: all 0.2s; ${style} width: 100%; display: flex; align-items: center; gap: 0.75rem;">
                <span style="font-size: 1.4rem; color: #0369a1; line-height: 0;">•</span>
                <span style="font-size: 0.95rem;">${std.name}</span>
            </button>
        `;
    }).join('');
}

async function loadStudentSubjectsForMarks(rollNo, name) {
    window.currentMarksRollNo = rollNo;
    const token = localStorage.getItem('token');
    const dept = document.getElementById('marksDept').value;
    const sem = document.getElementById('marksSemester').value;

    const tablePlaceholder = document.getElementById('marksTablePlaceholder');
    const marksContent = document.getElementById('marksEntryContent');
    if (tablePlaceholder) tablePlaceholder.style.display = 'none';
    if (marksContent) marksContent.style.display = 'block';

    document.getElementById('selectedStudentHeader').textContent = name;
    document.getElementById('selectedStudentMeta').textContent = `${dept} | Semester ${sem}`;

    const tableBody = document.getElementById('studentSubjectsBody');
    tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 2rem;"><i class="fas fa-spinner fa-spin"></i> Loading Subjects...</td></tr>';

    // Refresh list highlighting (without resetting UI)
    updateMarksFilters(true);

    try {
        // 1. Fetch all subjects for this dept/sem
        if (!window.allSubjectsList || window.allSubjectsList.length === 0) {
            const subResp = await fetch('/api/subjects', { headers: { 'Authorization': `Bearer ${token}` } });
            window.allSubjectsList = await subResp.json();
        }
        const subjects = window.allSubjectsList.filter(s => s.department === dept && s.semester === parseInt(sem));

        // 2. Fetch existing marks
        const mResp = await fetch(`/api/marks/student/${rollNo}/semester/${sem}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const existingMarks = await mResp.json();
        const marksMap = {};
        existingMarks.forEach(m => marksMap[m.subject] = m.marks);

        // 3. Render Table
        tableBody.innerHTML = subjects.length === 0 ?
            '<tr><td colspan="4" style="text-align: center; padding: 2rem; color: #94a3b8;">No subjects found for this Semester.</td></tr>' :
            subjects.map(s => `
                <tr class="subject-marks-row" style="border-bottom: 1px solid #f1f5f9;">
                    <td style="padding: 1rem;"><span style="background: #f1f5f9; padding: 0.2rem 0.6rem; border-radius: 6px; font-family: monospace; font-size: 0.85rem;">${s.code}</span></td>
                    <td style="padding: 1rem; font-weight: 600; color: #1e293b;">${s.name}</td>
                    <td style="padding: 1rem; text-align: center;">${s.credits}</td>
                    <td style="padding: 1rem;">
                        <input type="number" class="subject-marks-input form-control-flat" 
                               data-subject="${s.name}" 
                               value="${marksMap[s.name] !== undefined ? marksMap[s.name] : ''}" 
                               min="0" max="100" 
                               style="width: 100px; padding: 0.5rem; border-radius: 8px; font-weight: 600;"
                               placeholder="0-100">
                    </td>
                </tr>
            `).join('');
    } catch (error) {
        console.error('Error loading subjects for marks:', error);
        tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 2rem; color: #ef4444;">Error loading data.</td></tr>';
    }
}

async function saveBatchMarks() {
    const rows = document.querySelectorAll('.subject-marks-row');
    if (rows.length === 0) return;

    const rollNo = window.currentMarksRollNo;
    const sem = parseInt(document.getElementById('marksSemester').value);
    const token = localStorage.getItem('token');

    if (!rollNo) {
        alert('Please select a student first.');
        return;
    }

    const savePromises = [];
    rows.forEach(row => {
        const input = row.querySelector('.subject-marks-input');
        const subject = input.dataset.subject;
        const marksVal = input.value;

        if (marksVal !== '') {
            savePromises.push(fetch('/api/marks', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify({
                    student: { rollNo },
                    subject: subject,
                    marks: parseInt(marksVal),
                    semester: sem
                })
            }));
        }
    });

    if (savePromises.length === 0) {
        alert('Please enter marks for at least one subject.');
        return;
    }

    try {
        const results = await Promise.all(savePromises);
        if (results.every(r => r.ok)) {
            alert('✅ Marks saved successfully!');
            // Refresh table
            loadStudentSubjectsForMarks(rollNo, document.getElementById('selectedStudentHeader').textContent);
        } else {
            alert('⚠️ Some marks failed to save. Please check inputs.');
        }
    } catch (error) {
        console.error('Error saving batch marks:', error);
        alert('❌ Error: Could not save marks.');
    }
}

// Handle Subject Creation Form
const subForm = document.getElementById('subjectForm');
if (subForm) {
    subForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');

        const name = document.getElementById('subjName').value;
        const code = document.getElementById('subjCode').value;
        const department = document.getElementById('subjDept').value;
        const semester = document.getElementById('subjSem').value;
        const credits = document.getElementById('subjCredits').value;

        try {
            // First we need to fetch the teacher to link them
            const tResponse = await fetch(`/api/teachers/me`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!tResponse.ok) throw new Error('Could not fetch teacher details');
            const teacher = await tResponse.json();

            const response = await fetch('/api/subjects', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    name,
                    code,
                    department,
                    semester: parseInt(semester),
                    credits: parseInt(credits),
                    teacher: { facultyId: teacher.facultyId }
                })
            });
            if (response.ok) {
                alert('Subject created successfully!');
                subForm.reset();
                fetchAndDisplaySubjects(); // Refresh list
            } else {
                alert('Failed to create subject');
            }
        } catch (error) {
            console.error('Error creating subject:', error);
            alert('Error: ' + error.message);
        }
    });
}

// Handle Attendance Settings Form
const settingsForm = document.getElementById('attendanceSettingsForm');
if (settingsForm) {
    settingsForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');

        const windowData = {
            department: document.getElementById('att-dept-settings').value,
            semester: parseInt(document.getElementById('att-semester-settings').value),
            reopenDate: document.getElementById('att-reopen').value,
            lastWorkingDay: document.getElementById('att-last-day').value,
            periodsPerDay: parseInt(document.getElementById('att-periods').value)
        };

        try {
            const response = await fetch('/api/attendance/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(windowData)
            });
            if (response.ok) {
                const msg = await response.text();
                alert('✅ ' + msg);
                settingsForm.reset();
                fetchSemesterWindows();

            } else {
                alert('❌ Failed to generate attendance sheet');
            }
        } catch (error) {
            console.error('Error in attendance settings:', error);
            alert('Error: ' + error.message);
        }
    });
}

async function fetchTeacherProfile() {
    const token = localStorage.getItem('token');
    try {
        const response = await fetch('/api/teachers/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const t = await response.json();

            // Populate Header
            const cardGreetEl = document.getElementById('profileCardGreeting');
            if (cardGreetEl) cardGreetEl.textContent = `Welcome Back ${t.name}!`;
            const headerIdEl = document.getElementById('t-profile-id-header');
            if (headerIdEl) headerIdEl.textContent = `Faculty ID: ${t.facultyId}`;
            const headerEmailEl = document.getElementById('t-profile-email-header');
            if (headerEmailEl) headerEmailEl.textContent = t.email || 'Not Provided';

            // Populate Grid Details
            const nameDisplay = document.getElementById('t-profile-name');
            if (nameDisplay) nameDisplay.textContent = t.name;

            const idDisplay = document.getElementById('t-profile-id');
            if (idDisplay) idDisplay.textContent = t.facultyId;

            const emailEl = document.getElementById('t-profile-email');
            const phoneEl = document.getElementById('t-profile-phone');
            const specialEl = document.getElementById('t-profile-special');
            const deptEl = document.getElementById('t-profile-dept');
            const dobEl = document.getElementById('t-profile-dob');
            const genderEl = document.getElementById('t-profile-gender');
            const bloodEl = document.getElementById('t-profile-blood');
            const fatherEl = document.getElementById('t-profile-father');
            const motherEl = document.getElementById('t-profile-mother');
            const addressEl = document.getElementById('t-profile-address');

            if (emailEl) emailEl.textContent = t.email || 'Not Provided';
            if (phoneEl) phoneEl.textContent = t.phone || 'Not Provided';
            if (specialEl) specialEl.textContent = t.specialization || 'N/A';
            if (deptEl) deptEl.textContent = t.department || 'Not Assigned';
            if (dobEl) dobEl.textContent = t.dob || 'Not Provided';
            if (genderEl) genderEl.textContent = t.gender || 'Not Provided';
            if (bloodEl) bloodEl.textContent = t.bloodGroup || 'Not Provided';
            if (fatherEl) fatherEl.textContent = t.fatherName || 'Not Provided';
            if (motherEl) motherEl.textContent = t.motherName || 'Not Provided';
            if (addressEl) addressEl.textContent = t.address || 'Not Provided';
        }
    } catch (error) {
        console.error('Error fetching teacher profile:', error);
    }
}

function toggleTeacherProfileEdit(isEdit) {
    const editBtn = document.getElementById('edit-teacher-btn');
    const actions = document.getElementById('edit-teacher-actions');
    const fields = ['t-profile-email', 't-profile-phone', 't-profile-special', 't-profile-dob', 't-profile-gender', 't-profile-blood', 't-profile-father', 't-profile-mother', 't-profile-address'];

    if (isEdit) {
        if (editBtn) editBtn.style.display = 'none';
        if (actions) actions.style.display = 'flex';
        fields.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                const val = el.textContent === 'Not Provided' || el.textContent === 'N/A' ? '' : el.textContent;
                if (id === 't-profile-address') {
                    el.innerHTML = `<textarea class="form-control" style="width:100%; min-height:100px;">${val}</textarea>`;
                } else if (id === 't-profile-dob') {
                    el.innerHTML = `<input type="date" class="form-control" style="width:100%;" value="${val}">`;
                } else if (id === 't-profile-gender') {
                    el.innerHTML = `<select class="form-control" style="width:100%;">
                        <option value="">Select Gender</option>
                        <option value="Male" ${val === 'Male' ? 'selected' : ''}>Male</option>
                        <option value="Female" ${val === 'Female' ? 'selected' : ''}>Female</option>
                        <option value="Other" ${val === 'Other' ? 'selected' : ''}>Other</option>
                    </select>`;
                } else {
                    el.innerHTML = `<input type="text" class="form-control" style="width:100%;" value="${val}">`;
                }
            }
        });
    } else {
        if (editBtn) editBtn.style.display = 'block';
        if (actions) actions.style.display = 'none';
        fetchTeacherProfile();
    }
}


async function saveTeacherProfile() {
    const token = localStorage.getItem('token');
    const fields = {
        email: 't-profile-email',
        phone: 't-profile-phone',
        specialization: 't-profile-special',
        dob: 't-profile-dob',
        gender: 't-profile-gender',
        bloodGroup: 't-profile-blood',
        fatherName: 't-profile-father',
        motherName: 't-profile-mother',
        address: 't-profile-address'
    };

    const teacherDetails = {};
    Object.entries(fields).forEach(([key, id]) => {
        const el = document.getElementById(id);
        const input = el.querySelector('input, select, textarea');
        if (input) teacherDetails[key] = input.value;
    });

    try {
        const response = await fetch('/api/teachers/profile', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(teacherDetails)
        });

        if (response.ok) {
            alert('Profile updated successfully!');
            toggleTeacherProfileEdit(false);
        } else {
            const error = await response.text();
            alert('Failed to update profile: ' + error);
        }
    } catch (error) {
        console.error('Error updating profile:', error);
    }
}

// --- User Management (Admin Only) ---

function toggleUserForm() {
    const form = document.getElementById('userForm');
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

// Handle user creation form
const userForm = document.getElementById('addUserForm');
if (userForm) {
    userForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');

        const username = document.getElementById('u-username').value;
        const role = document.getElementById('u-role').value;
        const password = document.getElementById('u-password').value;
        const confirmPassword = document.getElementById('u-confirm-password').value;

        if (password !== confirmPassword) {
            alert('Passwords do not match!');
            return;
        }

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ username, password, role })
            });

            if (response.ok) {
                alert('User created successfully with temporary password!');
                userForm.reset();
                toggleUserForm();
            } else {
                const error = await response.text();
                alert('Failed to create user: ' + error);
            }
        } catch (error) {
            console.error('Error creating user:', error);
            alert('An error occurred. Please try again.');
        }
    });
}

// Attendance Settings Form Listener
const attendanceSettingsForm = document.getElementById('attendanceSettingsForm');
if (attendanceSettingsForm) {
    attendanceSettingsForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        const data = {
            department: document.getElementById('att-dept').value,
            semester: parseInt(document.getElementById('att-semester').value),
            reopenDate: document.getElementById('att-reopen').value,
            lastWorkingDay: document.getElementById('att-last-day').value,
            periodsPerDay: parseInt(document.getElementById('att-periods').value)
        };

        try {
            const response = await fetch('/api/attendance/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(data)
            });
            if (response.ok) {
                const msg = await response.text();
                alert(msg);
                attendanceSettingsForm.reset();
            } else {
                const error = await response.text();
                alert('Failed to generate sheet: ' + error);
            }
        } catch (error) {
            console.error('Error generating attendance:', error);
            alert('Error: ' + error.message);
        }
    });
}

// --- Student Dashboard Functions ---

// --- Section Switching Logic ---


async function fetchStudentStats() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    try {
        const response = await fetch(`/api/dashboard/student/${username}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            const attEl = document.getElementById('attendancePercentage');
            if (attEl) attEl.textContent = (data.attendancePercentage || '0') + '%';
            const examEl = document.getElementById('examResults');
            const gradeEl = document.getElementById('currentGrade');

            // Populate GPA/CGPA from Result API
            const resResponse = await fetch(`/api/results/student/${username}/overall`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (resResponse.ok) {
                const resData = await resResponse.json();
                const cgpa = (resData.cgpa || 0).toFixed(2);
                const cgpaTextEl = document.getElementById('cgpa-text');
                if (cgpaTextEl) cgpaTextEl.textContent = cgpa;

                const cgpaRingEl = document.getElementById('cgpa-ring');
                if (cgpaRingEl) {
                    const percentage = (resData.cgpa * 10) || 0; // Scale 0-10 to 0-100
                    const offset = 157 - (157 * percentage / 100);
                    cgpaRingEl.style.strokeDashoffset = offset;
                }

                if (examEl) examEl.textContent = `${Object.keys(resData.semesterWiseGPA || {}).length} Semesters`;
                if (gradeEl) gradeEl.textContent = cgpa >= 9 ? 'O' : cgpa >= 8 ? 'A+' : cgpa >= 7 ? 'A' : cgpa >= 6 ? 'B' : 'C';
            }
        }
    } catch (error) {
        console.error('Error fetching student stats:', error);
    }
}

async function fetchStudentProfile() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    try {
        const response = await fetch(`/api/students/${username}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const std = await response.json();

            // Header fields
            const nameEl = document.getElementById('display-name');
            if (nameEl) nameEl.textContent = std.name;
            const emailEl = document.getElementById('display-email');
            if (emailEl) emailEl.textContent = std.email;

            // Banner Greeting
            const greetEl = document.getElementById('userGreeting');
            if (greetEl) greetEl.textContent = `Welcome Back ${std.name}!`;

            // Profile Card Greeting
            const cardGreetEl = document.getElementById('profileCardGreeting');
            if (cardGreetEl) cardGreetEl.textContent = `Welcome Back ${std.name}!`;

            const commonRollEl = document.getElementById('profile-roll');
            if (commonRollEl) commonRollEl.textContent = `Roll No: ${std.rollNo}`;

            // Profile Section Fields
            const fieldMap = {
                'profile-full-name': std.name,
                'profile-roll': `Roll No: ${std.rollNo}`,
                'profile-email': std.email || 'Not Provided',
                'profile-phone': std.phone || 'Not Provided',
                'profile-dob': std.dob || 'N/A',
                'profile-gender': std.gender || 'N/A',
                'profile-dept': std.department || 'Not Assigned',
                'profile-year': std.studentClass || std.year || 'N/A',
                'profile-blood': std.bloodGroup || 'N/A',
                'profile-father': std.fatherName || 'Not Provided',
                'profile-mother': std.motherName || 'Not Provided',
                'profile-address': std.address || 'Not Provided'
            };

            Object.entries(fieldMap).forEach(([id, value]) => {
                const el = document.getElementById(id);
                if (el) el.textContent = value;
            });
        }
    } catch (error) {
        console.error('Error fetching student profile:', error);
    }
}

function toggleProfileEdit(isEdit) {
    const editBtn = document.getElementById('edit-profile-btn');
    const actions = document.getElementById('edit-profile-actions');
    const fields = [
        'profile-email', 'profile-phone', 'profile-dob', 'profile-gender',
        'profile-blood', 'profile-father', 'profile-mother', 'profile-address'
    ];

    if (isEdit) {
        if (editBtn) editBtn.style.display = 'none';
        if (actions) actions.style.display = 'flex';
        fields.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                const val = el.textContent === 'Not Provided' || el.textContent === 'N/A' ? '' : el.textContent;
                if (id === 'profile-address') {
                    el.innerHTML = `<textarea class="form-control" style="width:100%;">${val}</textarea>`;
                } else if (id === 'profile-gender') {
                    el.innerHTML = `
                        <select class="form-control">
                            <option value="Male" ${val === 'Male' ? 'selected' : ''}>Male</option>
                            <option value="Female" ${val === 'Female' ? 'selected' : ''}>Female</option>
                            <option value="Other" ${val === 'Other' ? 'selected' : ''}>Other</option>
                        </select>`;
                } else if (id === 'profile-dob') {
                    el.innerHTML = `<input type="date" class="form-control" value="${val}">`;
                } else {
                    el.innerHTML = `<input type="text" class="form-control" value="${val}">`;
                }
            }
        });
    } else {
        if (editBtn) editBtn.style.display = 'block';
        if (actions) actions.style.display = 'none';
        fetchStudentProfile();
    }
}

async function saveProfile() {
    const token = localStorage.getItem('token');
    const fields = {
        email: 'profile-email',
        phone: 'profile-phone',
        dob: 'profile-dob',
        gender: 'profile-gender',
        bloodGroup: 'profile-blood',
        fatherName: 'profile-father',
        motherName: 'profile-mother',
        address: 'profile-address'
    };

    const studentDetails = {};
    Object.entries(fields).forEach(([key, id]) => {
        const el = document.getElementById(id);
        const input = el.querySelector('input, select, textarea');
        if (input) studentDetails[key] = input.value;
    });

    try {
        const response = await fetch('/api/students/profile', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(studentDetails)
        });

        if (response.ok) {
            alert('Profile updated successfully!');
            toggleProfileEdit(false);
        } else {
            const error = await response.text();
            alert('Failed to update profile: ' + error);
        }
    } catch (error) {
        console.error('Error updating profile:', error);
    }
}


async function fetchStudentAttendance() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const sem = document.getElementById('attendanceSemester')?.value || 1;
    const tableBody = document.getElementById('attendanceDashboardBody');
    if (!tableBody) return;

    try {
        const response = await fetch(`/api/attendance/student/${username}/summary/semester/${sem}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            console.log('Attendance data loaded:', data);

            const setSafeText = (id, val) => {
                const el = document.getElementById(id);
                if (el) el.textContent = val;
            };

            setSafeText('att-present', data.presentHours);
            setSafeText('att-absent', data.absentHours);
            setSafeText('att-worked', data.workedHours);
            setSafeText('att-percent', (data.attendancePercentage || "0") + "%");
            setSafeText('att-od-val', data.onDutyHours);
            setSafeText('att-ml-val', data.medicalLeaveHours);

            if (document.getElementById('att-open-date'))
                document.getElementById('att-open-date').textContent = data.enrollmentDate || 'N/A';
            if (document.getElementById('att-close-date'))
                document.getElementById('att-close-date').textContent = data.endDate || 'N/A';

            const eligibilityEl = document.getElementById('attendance-eligibility');
            if (eligibilityEl) {
                eligibilityEl.textContent = data.eligible ? '🟢 ELIGIBLE' : '🔴 NOT ELIGIBLE';
                eligibilityEl.style.background = data.eligible ? '#dcfce7' : '#fee2e2';
                eligibilityEl.style.color = data.eligible ? '#15803d' : '#b91c1c';
            }

            const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

            tableBody.innerHTML = data.dailyAttendance.length === 0 ?
                `<tr><td colspan="15" style="padding: 2rem; color: var(--text-muted);">No records for Semester ${sem}</td></tr>` :
                data.dailyAttendance.map(row => {
                    let formattedDate = row.date;
                    try {
                        const d = new Date(row.date);
                        if (!isNaN(d.getTime())) {
                            formattedDate = `${d.getDate()}-${months[d.getMonth()]}-${d.getFullYear()}`;
                        }
                    } catch (e) { }

                    let periodsHtml = '';
                    const periods = data.periodsPerDay || 8;
                    for (let i = 1; i <= periods; i++) {
                        const status = row['P' + i] || '–';
                        const isHoliday = status === '–';

                        periodsHtml += `<td style="padding: 1rem;">
                            <span style="width: 28px; height: 28px; border-radius: 4px; background: ${getStatusColor(status)}; color: ${isHoliday ? '#94a3b8' : 'white'}; display: inline-flex; align-items: center; justify-content: center; font-weight: bold; font-size: 0.75rem;">
                                ${status}
                            </span>
                        </td>`;
                    }
                    return `<tr style="border-bottom: 1px solid #f1f5f9;">
                        <td style="padding: 1rem; font-weight: 600;">${formattedDate}</td>
                        <td style="padding: 1rem;">${row.day}</td>
                        <td style="padding: 1rem;">${row.description || '–'}</td>
                        <td style="padding: 1rem; color: #64748b;">${row.dayType}</td>
                        ${periodsHtml}
                    </tr>`;
                }).join('');
        }
    } catch (error) {
        console.error('Error fetching attendance:', error);
        if (tableBody) tableBody.innerHTML = `<tr><td colspan="15" style="padding: 2rem; color: #ef4444;">Error loading attendance data.</td></tr>`;
    }
}

function getStatusColor(status) {
    switch (status) {
        case 'P': return '#22c55e';
        case 'A': return '#ef4444';
        case 'OD': return '#3b82f6';
        case 'ML': return '#f59e0b';
        case 'NT': return '#94a3b8';
        case '–': return '#e2e8f0';
        default: return '#e2e8f0';
    }
}

async function fetchAiInsights() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const content = document.getElementById('aiInsightsContent');
    if (!content) return;

    try {
        const response = await fetch(`/api/dashboard/ai-insights/${username}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const insights = await response.json();
            content.innerHTML = `<p><strong>Prediction:</strong> ${insights.prediction}</p>
                                 <p style="margin-top: 0.5rem;"><strong>Suggestion:</strong> ${insights.suggestion}</p>`;
        }
    } catch (error) {
        console.error('Error fetching AI insights:', error);
    }
}

async function fetchSemesterResults() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const sem = document.getElementById('resultSemester')?.value;
    const tableBody = document.getElementById('semesterResultsBody');
    if (!sem || !tableBody) return;

    try {
        const response = await fetch(`/api/results/student/${username}/semester/${sem}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            console.log('Semester Results loaded:', data);
            const gpaEl = document.getElementById('sem-gpa');
            if (gpaEl) gpaEl.textContent = (data.gpa || 0).toFixed(2);

            let totalCredits = 0;
            tableBody.innerHTML = data.subjects && data.subjects.length > 0 ?
                data.subjects.map(s => {
                    totalCredits += s.credits;
                    return `<tr style="border-bottom: 1px solid #f1f5f9;">
                        <td style="padding: 1rem; font-weight: 600;">${s.subjectName}</td>
                        <td style="padding: 1rem;">${s.credits}</td>
                        <td style="padding: 1rem;">${s.marks}</td>
                        <td style="padding: 1rem;"><span class="badge ${s.grade === 'F' ? 'badge-danger' : 'badge-success'}">${s.grade}</span></td>
                    </tr>`;
                }).join('') :
                '<tr><td colspan="4" style="padding: 2rem; text-align: center; color: #94a3b8;">No results found for Semester ' + sem + '.</td></tr>';

            const credEl = document.getElementById('sem-credits');
            if (credEl) credEl.textContent = totalCredits;

            // Sync Subject Progress Bars
            const progressList = document.getElementById('subjectProgressList');
            if (progressList) {
                progressList.innerHTML = data.subjects && data.subjects.length > 0 ?
                    data.subjects.map(s => {
                        const percentage = s.marks;
                        const barClass = percentage < 40 ? 'bg-red' : percentage < 75 ? 'bg-orange' : 'bg-green';
                        return `
                        <div class="homework-item">
                            <div class="homework-info">
                                <span>${s.subjectName}</span>
                                <span>${percentage}%</span>
                            </div>
                            <div class="progress-bar-container">
                                <div class="progress-bar ${barClass}" style="width: ${percentage}%;"></div>
                            </div>
                        </div>
                    `;
                    }).join('') :
                    '<div style="padding: 1.5rem; text-align: center; color: var(--text-muted);">No progress data for Semester ' + sem + '.</div>';
            }
        } else {
            tableBody.innerHTML = '<tr><td colspan="4" style="padding: 2rem; text-align: center; color: #ef4444;">Failed to load results.</td></tr>';
        }
    } catch (error) {
        console.error('Error fetching results:', error);
        if (tableBody) tableBody.innerHTML = '<tr><td colspan="4" style="padding: 2rem; text-align: center; color: #ef4444;">Error loading results.</td></tr>';
    }
}



async function fetchDepartmentAverages() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('deptAveragesBody');
    if (!tableBody) return;

    try {
        const response = await fetch('/api/results/department-averages', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            tableBody.innerHTML = Object.entries(data).map(([dept, avg]) => {
                const badgeClass = avg >= 8 ? 'badge-success' : avg >= 6 ? 'badge-info' : 'badge-danger';
                const status = avg >= 8 ? 'Excellent' : avg >= 6 ? 'Good' : 'Needs Improvement';
                return `
                    <tr>
                        <td>${dept}</td>
                        <td>${avg.toFixed(2)}</td>
                        <td><span class="badge ${badgeClass}" style="padding: 0.4rem 0.8rem; border-radius: 8px; font-weight: 600;">${status}</span></td>
                    </tr>
                `;
            }).join('');
        }
    } catch (error) {
        console.error('Error fetching department averages:', error);
    }
}

// ========================================
// ATTENDANCE MARKING FUNCTIONS (TEACHER)
// ========================================

async function loadStudentsForAttendance() {
    const token = localStorage.getItem('token');
    const dept = document.getElementById('att-dept')?.value;
    const semester = document.getElementById('att-semester')?.value;
    const date = document.getElementById('att-date')?.value;
    const tableBody = document.getElementById('attendanceStudentList');

    if (!tableBody) return;

    if (!dept || !semester || !date) {
        tableBody.innerHTML = `<tr><td colspan="10" style="padding: 5rem; text-align: center; color: #94a3b8;">
            <i class="fas fa-info-circle"></i> Please select Department, Semester and Date to load register
        </td></tr>`;
        return;
    }

    tableBody.innerHTML = `<tr><td colspan="10" style="padding: 5rem; text-align: center; color: #94a3b8;">
        <i class="fas fa-spinner fa-spin"></i> Loading Register...
    </td></tr>`;

    try {
        console.log(`[Diagnostic] Filtering students for Dept: "${dept}", Sem: "${semester}"`);

        // 1. Fetch Students
        const sResp = await fetch('/api/students', { headers: { 'Authorization': `Bearer ${token}` } });
        const allStudents = await sResp.json();

        console.log(`[Diagnostic] Total students in DB: ${allStudents.length}`);

        const students = allStudents.filter(s => {
            const dMatch = s.department === dept;
            const sMatch = s.semester === parseInt(semester);
            if (dMatch && !sMatch) console.log(`[Diagnostic] Student ${s.rollNo} matches Dept but Sem is ${s.semester} (expected ${semester})`);
            return dMatch && sMatch;
        });

        console.log(`[Diagnostic] Matched students: ${students.length}`);

        // 2. Fetch existing attendance for this date
        const aResp = await fetch(`/api/attendance/date/${date}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const dailyAtt = aResp.ok ? await aResp.json() : [];

        // Map attendance: rollNo -> period -> status
        const attMap = {};
        dailyAtt.forEach(a => {
            const roll = a.student.rollNo;
            if (!attMap[roll]) attMap[roll] = {};
            attMap[roll][a.period] = a.status;
        });

        if (students.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="10" style="padding: 5rem; text-align: center; color: #94a3b8;">
                <div style="background: #fff1f2; color: #991b1b; padding: 2rem; border-radius: 12px; border: 1px solid #fecaca;">
                    <i class="fas fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 1rem;"></i>
                    <h3 style="margin: 0;">No matching students found</h3>
                    <p style="margin: 0.5rem 0 0;">Check if students are registered for <strong>${dept}</strong> and <strong>Semester ${semester}</strong>.</p>
                </div>
            </td></tr>`;
            return;
        }

        tableBody.innerHTML = students.map(s => {
            let periodsHtml = '';
            for (let p = 1; p <= 8; p++) {
                const currentStatus = attMap[s.rollNo]?.[p] || 'P'; // Default to Present
                periodsHtml += `
                    <td style="padding: 0.75rem; text-align: center;">
                        <select class="period-select theme-select" data-roll="${s.rollNo}" data-period="${p}" 
                                style="width: 50px; padding: 0.35rem; border-radius: 8px; border: 2px solid #e2e8f0; font-weight: 800; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; ${getStatusColor(currentStatus)}">
                            <option value="P" ${currentStatus === 'P' ? 'selected' : ''}>P</option>
                            <option value="A" ${currentStatus === 'A' ? 'selected' : ''}>A</option>
                            <option value="OD" ${currentStatus === 'OD' ? 'selected' : ''}>OD</option>
                            <option value="ML" ${currentStatus === 'ML' ? 'selected' : ''}>ML</option>
                        </select>
                    </td>
                `;
            }
            return `
                <tr style="border-bottom: 1px solid #f1f5f9; transition: background 0.2s;" onmouseover="this.style.background='#f0f9ff'" onmouseout="this.style.background='white'">
                    <td style="padding: 1rem 1.5rem; font-weight: 800; color: #0f172a; border-left: 4px solid #3b82f6;">${s.rollNo}</td>
                    <td style="padding: 1rem 1.5rem; font-weight: 600; color: #334155;">${s.name}</td>
                    ${periodsHtml}
                </tr>
            `;
        }).join('');

        // Add change listeners for colors
        document.querySelectorAll('.period-select').forEach(sel => {
            sel.addEventListener('change', (e) => {
                const status = e.target.value;
                e.target.style.cssText = `width: 50px; padding: 0.35rem; border-radius: 8px; border: 2px solid #e2e8f0; font-weight: 800; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; ${getStatusColor(status)}`;
            });
        });

    } catch (error) {
        console.error('Error loading attendance register:', error);
        tableBody.innerHTML = '<tr><td colspan="10" style="padding: 5rem; text-align: center; color: #ef4444;">Error loading register. Check console for details.</td></tr>';
    }
}

function getStatusColor(status) {
    if (status === 'P') return 'background: #f0fdf4; color: #166534; border-color: #bbf7d0;';
    if (status === 'A') return 'background: #fef2f2; color: #991b1b; border-color: #fecaca;';
    if (status === 'OD') return 'background: #eff6ff; color: #1e40af; border-color: #bfdbfe;';
    if (status === 'ML') return 'background: #fffbeb; color: #92400e; border-color: #fde68a;';
    return '';
}

function markGridStatus(status) {
    document.querySelectorAll('.period-select').forEach(sel => {
        sel.value = status;
        sel.style.cssText = `width: 50px; padding: 0.35rem; border-radius: 8px; border: 2px solid #e2e8f0; font-weight: 800; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; ${getStatusColor(status)}`;
    });
}

async function saveAttendanceGrid() {
    const token = localStorage.getItem('token');
    const date = document.getElementById('att-date')?.value;
    const sem = parseInt(document.getElementById('att-semester')?.value);
    const selects = document.querySelectorAll('.period-select');

    if (!selects.length) return;

    // Premium confirmation dialog
    if (!confirm(`Are you sure you want to save attendance for ${selects.length / 8} students?`)) return;

    const saveBtn = document.querySelector('button[onclick="saveAttendanceGrid()"]');
    const originalText = saveBtn.innerHTML;
    saveBtn.innerHTML = '<i class="fas fa-circle-notch fa-spin"></i> Processing...';
    saveBtn.disabled = true;

    try {
        const batch = [];
        selects.forEach(sel => {
            batch.push({
                student: { rollNo: sel.dataset.roll },
                date: date,
                period: parseInt(sel.dataset.period),
                semester: sem,
                status: sel.value
            });
        });

        // Use sequential or chunked batching if needed, but for small classes Parallel is fine
        const promises = batch.map(data => fetch('/api/attendance', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(data)
        }));

        const results = await Promise.all(promises);
        if (results.every(r => r.ok)) {
            alert('✅ Attendance Register saved successfully!');
        } else {
            alert('⚠️ Some records failed to save. Please try again.');
        }
    } catch (error) {
        console.error('Error saving attendance grid:', error);
        alert('❌ Network error while saving attendance.');
    } finally {
        saveBtn.innerHTML = originalText;
        saveBtn.disabled = false;
        loadStudentsForAttendance();
    }
}


// Teacher Leave Approval Functions
async function fetchPendingLeaveRequests() {
    const token = localStorage.getItem('token');
    const badge = document.getElementById('leave-notification-badge');
    const tableBody = document.getElementById('pendingLeaveRequestsBody');

    try {
        const response = await fetch('/api/leave-requests/pending', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const requests = await response.json();
            if (tableBody) displayPendingRequests(requests);

            // Update Notification Badge
            if (badge) {
                const count = requests.length;
                if (count > 0) {
                    badge.textContent = count;
                    badge.style.display = 'inline-block';
                } else {
                    badge.style.display = 'none';
                }
            }
        }
    } catch (error) {
        console.error('Error fetching pending requests:', error);
    }
}

function displayPendingRequests(requests) {
    const tableBody = document.getElementById('pendingLeaveRequestsBody');
    if (!tableBody) return;

    if (requests.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="padding: 2rem; text-align: center; color: var(--text-muted);">No pending leave requests found</td></tr>';
        return;
    }

    tableBody.innerHTML = requests.map(req => `
        <tr style="border-bottom: 1px solid #f1f5f9;">
            <td style="padding: 1rem;">
                <div style="font-weight: 600;">${req.studentName}</div>
                <div style="font-size: 0.75rem; color: #64748b;">${req.studentRollNo}</div>
            </td>
            <td style="padding: 1rem;"><span class="badge ${req.requestType.toLowerCase()}">${req.requestType}</span></td>
            <td style="padding: 1rem; font-size: 0.85rem;">${req.fromDate} to ${req.toDate}</td>
            <td style="padding: 1rem; font-size: 0.85rem;">${req.reason}</td>
            <td style="padding: 1rem;">${req.numberOfDays}</td>
            <td style="padding: 1rem;">
                <div style="display: flex; gap: 0.5rem;">
                    <button class="btn btn-primary" onclick="processLeaveRequest(${req.id}, 'approve')" style="padding: 0.4rem 0.8rem; font-size: 0.75rem; background: #22c55e;">
                        <i class="fas fa-check"></i>
                    </button>
                    <button class="btn btn-primary" onclick="processLeaveRequest(${req.id}, 'reject')" style="padding: 0.4rem 0.8rem; font-size: 0.75rem; background: #ef4444;">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function processLeaveRequest(id, action) {
    const token = localStorage.getItem('token');
    const remarks = prompt(`Enter remarks for ${action}:`);
    if (remarks === null) return; // User cancelled

    try {
        const response = await fetch(`/api/leave-requests/${id}/${action}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ remarks: remarks })
        });

        if (response.ok) {
            alert(`✅ Request ${action}d successfully!`);
            fetchPendingLeaveRequests();
        } else {
            alert('❌ Operation failed');
        }
    } catch (error) {
        console.error(`Error processing leave request ${action}:`, error);
        alert('❌ Error communicating with server');
    }
}

// --- Student Leave Request Functions ---

async function fetchStudentLeaveRequests() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const tableBody = document.getElementById('leaveRequestsBody');
    if (!tableBody) return;

    try {
        const response = await fetch(`/api/leave-requests/student/${username}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const requests = await response.json();
            console.log('Leave requests loaded:', requests);
            window.allLeaveRequests = requests;
            displayLeaveRequests(requests);
        } else {
            tableBody.innerHTML = '<tr><td colspan="10" style="padding: 2rem; text-align: center; color: #ef4444;">Failed to load leave requests.</td></tr>';
        }
    } catch (error) {
        console.error('Error fetching leave requests:', error);
        tableBody.innerHTML = '<tr><td colspan="10" style="padding: 2rem; text-align: center; color: #ef4444;">Error loading leave requests.</td></tr>';
    }
}

function displayLeaveRequests(requests) {
    const tableBody = document.getElementById('leaveRequestsBody');
    if (!tableBody) return;

    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const formatDate = (dateStr) => {
        try {
            const d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return `${d.getDate()}-${months[d.getMonth()]}-${d.getFullYear()}`;
        } catch (e) { return dateStr; }
    };

    tableBody.innerHTML = requests.length === 0 ?
        '<tr><td colspan="10" style="padding: 2rem; text-align: center; color: #94a3b8;">No leave requests found</td></tr>' :
        requests.map((req, index) => `
            <tr style="border-bottom: 1px solid #f1f5f9; transition: background 0.2s;" onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background='transparent'">
                <td style="padding: 1rem; color: #64748b; font-weight: 600;">#${index + 1}</td>
                <td style="padding: 1rem;"><span class="badge ${req.requestType.toLowerCase()}" style="padding: 0.4rem 0.8rem; border-radius: 8px; font-weight: 600; font-size: 0.75rem;">${req.requestType}</span></td>
                <td style="padding: 1rem;"><div style="font-weight: 600; color: #1e293b;">${req.teacherName || 'General'}</div></td>
                <td style="padding: 1rem; color: #475569;">${formatDate(req.fromDate)}</td>
                <td style="padding: 1rem; color: #475569;">${formatDate(req.toDate)}</td>
                <td style="padding: 1rem;"><span style="background: #f1f5f9; padding: 0.25rem 0.6rem; border-radius: 6px; font-weight: 600; color: #475569;">${req.numberOfDays}</span></td>
                <td style="padding: 1rem; color: #64748b; font-size: 0.85rem;">${formatDate(req.appliedOn)}</td>
                <td style="padding: 1rem; color: #475569; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${req.reason}">${req.reason}</td>
                <td style="padding: 1rem;"><span class="badge-status ${req.status.toLowerCase()}" style="padding: 0.4rem 0.8rem; border-radius: 20px; font-weight: 700; font-size: 0.7rem; text-transform: uppercase;">${req.status}</span></td>
                <td style="padding: 1rem; color: #64748b; font-style: italic; font-size: 0.85rem;">${req.teacherRemarks || 'No remarks'}</td>
            </tr>
        `).join('');
}

async function applyForLeave(e) {
    e.preventDefault();
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');

    const leaveData = {
        student: { rollNo: username },
        teacher: { facultyId: document.getElementById('leave-staff').value },
        requestType: document.getElementById('leave-type').value,
        fromDate: document.getElementById('leave-from').value,
        toDate: document.getElementById('leave-to').value,
        reason: document.getElementById('leave-reason').value
    };

    if (!leaveData.teacher.facultyId) {
        alert('Please select a staff member first.');
        return;
    }

    try {
        const response = await fetch('/api/leave-requests', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(leaveData)
        });

        if (response.ok) {
            alert('✅ Leave request submitted successfully!');
            document.getElementById('leaveRequestForm').reset();
            document.getElementById('leave-dept').value = '';
            document.getElementById('leave-staff').innerHTML = '<option value="">-- Select Staff Member --</option>';
            document.getElementById('leave-staff').disabled = true;
            document.getElementById('leaveFormPanel').style.display = 'none';
            fetchStudentLeaveRequests();
        } else {
            const error = await response.json();
            alert('❌ Failed to submit: ' + (error.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error applying for leave:', error);
    }
}

async function loadStaffByDeptForLeave() {
    const dept = document.getElementById('leave-dept').value;
    const staffSelect = document.getElementById('leave-staff');
    const token = localStorage.getItem('token');

    if (!dept) {
        staffSelect.innerHTML = '<option value="">-- Select Staff Member --</option>';
        staffSelect.disabled = true;
        document.getElementById('leaveFormPanel').style.display = 'none';
        return;
    }

    try {
        staffSelect.innerHTML = '<option value="">Loading staff...</option>';
        const response = await fetch(`/api/teachers/department/${encodeURIComponent(dept)}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const staff = await response.json();
            staffSelect.innerHTML = '<option value="">-- Select Staff Member --</option>' +
                staff.map(s => `<option value="${s.facultyId}">${s.name} (${s.specialization || 'Faculty'})</option>`).join('');
            staffSelect.disabled = false;
        } else {
            staffSelect.innerHTML = `<option value="">Failed to load staff (Error ${response.status})</option>`;
        }
    } catch (error) {
        console.error('Error loading staff:', error);
        staffSelect.innerHTML = '<option value="">Network error loading staff</option>';
    }
}

// Add event listener for staff selection to show form
document.addEventListener('change', (e) => {
    if (e.target.id === 'leave-staff') {
        const panel = document.getElementById('leaveFormPanel');
        if (e.target.value) {
            panel.style.display = 'block';
            panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } else {
            panel.style.display = 'none';
        }
    }
});

function filterLeaveRequests() {
    if (!window.allLeaveRequests) return;
    const type = document.getElementById('filter-leave-type').value;
    const status = document.getElementById('filter-status').value;
    let filtered = window.allLeaveRequests;
    if (type) filtered = filtered.filter(r => r.requestType === type);
    if (status) filtered = filtered.filter(r => r.status === status);
    displayLeaveRequests(filtered);
}



// Navigation and User Actions
// Navigation and User Actions
function showSection(sectionId, link) {
    console.log('Switching to section:', sectionId);
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.style.display = 'none';
        console.log('Hiding section:', sec.id);
    });

    const target = document.getElementById(sectionId);
    if (target) {
        target.style.display = 'block';
        console.log('Target section shown:', sectionId);

        // Trigger data fetch
        if (sectionId === 'dashboard-section' && typeof fetchStats === 'function') {
            fetchStats();
            if (typeof fetchDepartmentAverages === 'function') fetchDepartmentAverages();
        }
        if (sectionId === 'students-section' && typeof fetchStudents === 'function') fetchStudents();
        if (sectionId === 'teachers-section' && typeof fetchTeachers === 'function') fetchTeachers();

        if (sectionId === 'results-section' && typeof fetchSemesterResults === 'function') fetchSemesterResults();
        if (sectionId === 'attendance-section' && typeof fetchStudentAttendance === 'function') fetchStudentAttendance();
        if (sectionId === 'leave-request-section' && typeof fetchStudentLeaveRequests === 'function') fetchStudentLeaveRequests();
        if (sectionId === 'subjects' && typeof fetchAndDisplaySubjects === 'function') fetchAndDisplaySubjects();
        if (sectionId === 'marks' && typeof updateMarksFilters === 'function') updateMarksFilters();
        if (sectionId === 'attendance' && typeof loadStudentsForAttendance === 'function') loadStudentsForAttendance();
    } else {
        console.warn('Section target not found:', sectionId);
    }

    if (link) {
        document.querySelectorAll('.sidebar-link').forEach(l => l.classList.remove('active'));
        link.classList.add('active');
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.href = 'login.html';
}

function toggleProfileEdit(enable) {
    const editBtn = document.getElementById('edit-profile-btn');
    const actions = document.getElementById('edit-profile-actions');
    const formDisplay = enable ? 'none' : 'inline-block';
    const actionDisplay = enable ? 'flex' : 'none';
    if (editBtn) editBtn.style.display = formDisplay;
    if (actions) actions.style.display = actionDisplay;

    // Toggle inputs
    const fieldMap = ['profile-email', 'profile-phone', 'profile-address', 'profile-dob', 'profile-blood', 'profile-father', 'profile-mother'];
    if (enable) {
        fieldMap.forEach(id => {
            const el = document.getElementById(id);
            if (el && !el.querySelector('input')) {
                const val = el.textContent;
                el.innerHTML = `<input type="text" class="form-control" value="${val}" style="width:100%">`;
            }
        });
    } else {
        // Revert/Refresh
        if (typeof fetchStudentProfile === 'function') fetchStudentProfile();
    }
}


function saveProfile() {
    if (typeof updateProfile === 'function') {
        updateProfile();
    } else {
        console.error('updateProfile missing. Using default saveProfile logic.');
        // Fallback to the existing saveProfile implementation if it exists, 
        // but here we already have a saveProfile at 899. 
        // To avoid conflicts, I'll ensure only one exists.
    }
}

// ==========================================
// Academic Calendar / Semester Windows Logic
// ==========================================

async function fetchSemesterWindows() {
    const token = localStorage.getItem('token');
    const tableBody = document.getElementById('semesterWindowsBody');
    // If the table doesn't exist (e.g. strict student view), just return
    if (!tableBody) return;

    try {
        const response = await fetch('/api/attendance/windows', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const windows = await response.json();

            if (windows.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" style="padding: 1.5rem; text-align: center; color: #94a3b8;">No dates scheduled yet.</td></tr>';
                return;
            }

            // Sort: Dept A-Z -> Semester 1-8
            windows.sort((a, b) => {
                if (a.department !== b.department) return a.department.localeCompare(b.department);
                return a.semester - b.semester;
            });

            tableBody.innerHTML = windows.map(w => `
                <tr style="border-bottom: 1px solid #f1f5f9;">
                    <td style="padding: 1rem; color: #334155; font-weight: 500;">${w.department}</td>
                    <td style="padding: 1rem; color: #64748b;">${toRomanSemester(w.semester)}</td>
                    <td style="padding: 1rem; color: #059669; font-weight: 500;">${formatDateDDMMYYYY(w.reopenDate)}</td>
                    <td style="padding: 1rem; color: #dc2626; font-weight: 500;">${formatDateDDMMYYYY(w.lastWorkingDay)}</td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Error fetching semester windows:', error);
        tableBody.innerHTML = '<tr><td colspan="4" style="padding: 1.5rem; text-align: center; color: #ef4444;">Failed to load data.</td></tr>';
    }
}

function toRomanSemester(num) {
    const romans = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII"];
    return num >= 1 && num <= 8 ? `Semester ${romans[num - 1]}` : `Semester ${num}`;
}

function formatDateDDMMYYYY(dateString) {
    if (!dateString) return '-';
    const [year, month, day] = dateString.split('-');
    return `${day}/${month}/${year}`;
}


// ==========================================
// Attendance Generation Form Handler
// ==========================================

async function handleAttendanceGeneration(event) {
    event.preventDefault();
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    submitBtn.disabled = true;

    const formData = {
        department: document.getElementById('att-dept-settings').value,
        semester: parseInt(document.getElementById('att-semester-settings').value),
        reopenDate: document.getElementById('att-reopen').value,
        lastWorkingDay: document.getElementById('att-last-day').value,
        periodsPerDay: parseInt(document.getElementById('att-periods').value)
    };

    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/attendance/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(formData)
        });

        const msg = await response.text();
        if (response.ok) {
            alert('✅ ' + msg);
            fetchSemesterWindows();
        } else {
            alert('❌ Error: ' + msg);
        }
    } catch (error) {
        console.error('Attendance generation error:', error);
        alert('❌ Failed to connect to server.');
    } finally {
        submitBtn.innerHTML = originalBtnText;
        submitBtn.disabled = false;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const attForm = document.getElementById('attendanceSettingsForm');
    if (attForm) {
        attForm.addEventListener('submit', handleAttendanceGeneration);
    }
});
