package wlceligue.admin.webapp.service

import javax.validation.constraints.Min

data class CupDraw(
		
		@Min(0)
		var round: Int? = null,
		
		@Min(0)
		var day: Int? = null,
		
		@Min(0)
		var user: Int? = null,

		@Min(0)
		var team: Int? = null,

		var local: Boolean = false
)