CREATE TABLE IF NOT EXISTS app_user(
                                       id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL,
    department VARCHAR(64),
    manager_id VARCHAR(64)
    );

CREATE TABLE IF NOT EXISTS staff(
                                    id VARCHAR(64) PRIMARY KEY,
    full_name VARCHAR(128) NOT NULL,
    department VARCHAR(64),
    manager_id VARCHAR(64),
    annual_leave_allocation INT NOT NULL
    );

CREATE TABLE IF NOT EXISTS leave_request(
                                            id VARCHAR(64) PRIMARY KEY,
    staff_id VARCHAR(64) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATE NOT NULL
    );
