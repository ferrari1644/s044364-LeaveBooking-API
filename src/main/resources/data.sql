-- Users (identity/login)
INSERT INTO app_user(id,email,password_hash,role,department,manager_id)
VALUES
    ('u-admin','admin@email.com','$2a$10$H.d7xUSbdbqiV8S9vst2QeHcWPaXjTn9Xq3lD1q8xw1g5dN2lYV0O','ADMIN','HR',NULL),
    ('u-mgr','manager@email.com','$2a$10$H.d7xUSbdbqiV8S9vst2QeHcWPaXjTn9Xq3lD1q8xw1g5dN2lYV0O','MANAGER','IT',NULL),
    ('u-staff','staff@email.com','$2a$10$H.d7xUSbdbqiV8S9vst2QeHcWPaXjTn9Xq3lD1q8xw1g5dN2lYV0O','STAFF','IT','u-mgr');

-- Staff (we’ll use email == id for simple mapping in controller demo)
INSERT INTO staff(id, full_name, department, manager_id, annual_leave_allocation, leave_remaining)
VALUES
    ('admin@email.com','Alice Admin','HR',NULL,25,25),
    ('manager@email.com','Mark Manager','IT',NULL,25,25),
    ('staff@email.com','Sam Staff','IT','manager@email.com',25,25);
