-- V1.0.1__seed_users_people_students.sql

-- USERS
INSERT INTO users (username, email, password_hash, status)
VALUES
('nguyenvanan', 'an.nguyen@example.com', 'hashed_pw_1', 'ACTIVE'),
('tranthibich', 'bich.tran@example.com', 'hashed_pw_2', 'ACTIVE'),
('lehoangcuong', 'cuong.le@example.com', 'hashed_pw_3', 'ACTIVE'),
('phamthidiep', 'diep.pham@example.com', 'hashed_pw_4', 'ACTIVE'),
('vovanem', 'em.vo@example.com', 'hashed_pw_5', 'ACTIVE'),
('nguyenthichau', 'chau.nguyen@example.com', 'hashed_pw_6', 'ACTIVE'),
('giaminhdo', 'gia.do@example.com', 'hashed_pw_7', 'ACTIVE'),
('buithihoa', 'hoa.bui@example.com', 'hashed_pw_8', 'ACTIVE'),
('hoangvankien', 'kien.hoang@example.com', 'hashed_pw_9', 'ACTIVE'),
('phanthilan', 'lan.phan@example.com', 'hashed_pw_10', 'ACTIVE');

-- PEOPLE
INSERT INTO people (full_name, dob, phone, contact_email, address, user_id)
SELECT
    ps.full_name, ps.dob, ps.phone, ps.contact_email, ps.address, u.id
FROM (
    VALUES
    ('Nguyen Van An',   DATE '2007-05-12', '0905000001', 'an.nguyen@example.com',    'Hai Chau, Da Nang'),
    ('Tran Thi Bich',   DATE '2003-08-21', '0905000002', 'bich.tran@example.com',    'Hai Chau, Da Nang'),
    ('Le Hoang Cuong',  DATE '2005-02-10', '0905000003', 'cuong.le@example.com',     'Thanh Khe, Da Nang'),
    ('Pham Thi Diep',   DATE '2006-11-03', '0905000004', 'diep.pham@example.com',    'Cam Le, Da Nang'),
    ('Vo Van Em',       DATE '2004-04-19', '0905000005', 'em.vo@example.com',        'Hai Chau, Da Nang'),
    ('Nguyen Thi Chau', DATE '2004-10-09', '0905000006', 'chau.nguyen@example.com',  'Hai Chau, Da Nang'),
    ('Do Minh Gia',     DATE '2008-01-14', '0905000007', 'gia.do@example.com',       'Lien Chieu, Da Nang'),
    ('Bui Thi Hoa',     DATE '2002-12-30', '0905000008', 'hoa.bui@example.com',      'Hai Chau, Da Nang'),
    ('Hoang Van Kien',  DATE '2004-07-08', '0905000009', 'kien.hoang@example.com',   'Thanh Khe, Da Nang'),
    ('Phan Thi Lan',    DATE '2005-03-24', '0905000010', 'lan.phan@example.com',     'Hai Chau, Da Nang')
) AS ps(full_name, dob, phone, contact_email, address)
JOIN users u ON u.email = ps.contact_email;

-- STUDENTS
INSERT INTO students (person_id, student_code, enrollment_year)
SELECT
    p.id, ss.student_code, ss.enrollment_year
FROM (
    VALUES
    ('an.nguyen@example.com',   'STU001', 2023),
    ('bich.tran@example.com',   'STU002', 2023),
    ('cuong.le@example.com',    'STU003', 2022),
    ('diep.pham@example.com',   'STU004', 2024),
    ('em.vo@example.com',       'STU005', 2022),
    ('gia.do@example.com',      'STU006', 2024),
    ('kien.hoang@example.com',  'STU007', 2023)
) AS ss(contact_email, student_code, enrollment_year)
JOIN people p ON p.contact_email = ss.contact_email;
