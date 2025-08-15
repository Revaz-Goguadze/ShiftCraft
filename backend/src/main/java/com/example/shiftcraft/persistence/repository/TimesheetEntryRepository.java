package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, String> {
    
    List<TimesheetEntry> findByTimesheetId(String timesheetId);
    
    List<TimesheetEntry> findByAssignmentId(String assignmentId);
    
    List<TimesheetEntry> findByWorkDate(LocalDate workDate);
    
    List<TimesheetEntry> findByEntryType(TimesheetEntry.EntryType entryType);
    
    @Query("SELECT te FROM TimesheetEntry te WHERE te.timesheet.id = :timesheetId AND te.workDate BETWEEN :startDate AND :endDate")
    List<TimesheetEntry> findByTimesheetIdAndDateRange(@Param("timesheetId") String timesheetId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}