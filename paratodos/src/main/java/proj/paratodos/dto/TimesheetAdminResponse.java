package proj.paratodos.dto;

import java.util.List;

public record TimesheetAdminResponse(
        TimesheetAdminSummaryResponse summary,
        List<TimesheetAdminRowResponse> rows
) {
}