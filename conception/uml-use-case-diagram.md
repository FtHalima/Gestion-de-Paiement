# UML Use Case Diagram for Gestion Paiements Application

```mermaid
useCaseDiagram
    %% Actors
    actor "Professor/Teacher" as Professor
    actor "System User/User" as User
    actor "Administrator" as Admin
    
    %% System Boundary
    boundary "Gestion Paiements System" as System {
        %% Use Cases - Authentication
        usecase "Login" as UC1
        usecase "Logout" as UC2
        
        %% Use Cases - Professor Management
        usecase "Search Professor" as UC3
        usecase "Create Professor" as UC4
        usecase "Update Professor" as UC5
        usecase "Delete Professor" as UC6
        usecase "View Professor Details" as UC7
        
        %% Use Cases - Payment Management
        usecase "Create Payment" as UC8
        usecase "Edit Payment" as UC9
        usecase "Delete Payment" as UC10
        usecase "View Payment Details" as UC11
        usecase "Generate Payment PDF" as UC12
        usecase "Calculate Payment Amounts" as UC13
        usecase "Manage Travel Segments" as UC14
        
        %% Use Cases - Excel File Management
        usecase "Browse Excel Files" as UC15
        usecase "View Excel File Contents" as UC16
        usecase "Export Excel File" as UC17
        usecase "Archive Excel File" as UC18
        usecase "Reactivate Excel File" as UC19
        usecase "Delete Excel File" as UC20
        usecase "Rename Excel File" as UC21
        usecase "Append Payment to Excel" as UC22
        
        %% Use Cases - Dashboard & Reporting
        usecase "View Dashboard" as UC23
        usecase "View Statistics" as UC24
        usecase "View Recent Payments" as UC25
    }
    
    %% Relationships - Authentication
    User --> UC1 : initiates
    User --> UC2 : initiates
    
    %% Relationships - Professor Management
    User --> UC3 : initiates
    User --> UC4 : initiates
    User --> UC5 : <<extends>> UC4
    User --> UC6 : initiates
    User --> UC7 : <<includes>> UC3
    
    %% Relationships - Payment Management
    User --> UC8 : initiates
    User --> UC9 : <<extends>> UC8
    User --> UC10 : initiates
    User --> UC11 : <<includes>> UC8, UC9
    User --> UC12 : <<extends>> UC8, UC9
    User --> UC13 : <<includes>> UC8, UC9
    Professor --> UC8 : provides data for
    Professor --> UC9 : provides data for
    User --> UC14 : <<extends>> UC8
    
    %% Relationships - Excel File Management
    User --> UC15 : initiates
    User --> UC16 : <<includes>> UC15
    User --> UC17 : <<extends>> UC16
    User --> UC18 : initiates
    User --> UC19 : initiates
    User --> UC20 : initiates
    User --> UC21 : initiates
    UC22 : <<includes>> UC8, UC9
    
    %% Relationships - Dashboard & Reporting
    User --> UC23 : initiates
    User --> UC24 : <<includes>> UC23
    User --> UC25 : <<includes>> UC23
    
    %% Special Relationships
    note for UC4 "Create new professor record" end
    note for UC5 "Update existing professor information" end
    note for UC6 "Delete professor and optionally their payments" end
    note for UC8 "Create new payment for selected professor" end
    note for UC9 "Modify existing payment details" end
    note for UC10 "Delete payment from system and Excel file" end
    note for UC11 "View detailed payment information" end
    note for UC12 "Generate PDF receipt for payment" end
    note for UC13 "Calculate amounts based on type, hours, rate, and IR%" end
    note for UC14 "Only applicable for DEPLACEMENT payment type" end
    note for UC15 "View list of active and archived Excel files by type" end
    note for UC16 "See preview of Excel file contents" end
    note for UC17 "Export Excel file to user-selected location" end
    note for UC18 "Move active Excel file to archive and create new active file" end
    note for UC19 "Move archived Excel file to active, archive current active if exists" end
    note for UC20 "Permanently delete Excel file (payments remain in DB)" end
    note for UC21 "Rename Excel file while preserving type prefix" end
    note for UC22 "Automatically append new payment to active Excel file" end
    note for UC23 "Main dashboard showing statistics and recent payments" end
    note for UC24 "Display counts by payment type (vacataire, heure sup, déplacement)" end
    note for UC25 "Show table of 4 most recent payments" end
    
    %% Notes
    note left of Professor
      Represents the professor/teacher
      for whom payments are managed
    end
    
    note right of User
      Represents the system user
      (administrator or staff member)
      who manages the system
    end
    
    note top of UC14
      Travel segment management
      is only available when
      creating/editing a 
      DEPLACEMENT payment
    end
```