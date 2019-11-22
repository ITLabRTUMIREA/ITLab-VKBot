package workwithapi

data class User(
    val id: String, val firstName: String, val lastName: String, val middleName: String?,
    val phoneNumber: String?, val email: String?,
    val properties: List<Property>
)

data class Property(val value: String, val status: String, val userPropertyType: PropertyType)

data class PropertyType(val id: String, val title: String, val description: String)
