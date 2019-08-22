package database.models

data class UserModel(val id: String, val email: String,val properties: List<Property>)

data class Property(
    val value: String,
    val status: String,
    val userPropertyType: UserPropertyType
)

data class UserPropertyType(val name: String)
