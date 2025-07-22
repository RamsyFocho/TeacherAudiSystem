### Insert the required roles
For example:

sql
Copy
Edit
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_DIRECTOR');
INSERT INTO roles (name) VALUES ('ROLE_USER');
### (Optional) Add role seeding on app startup
   To prevent this issue in the future, add a CommandLineRunner or ApplicationRunner in your Spring Boot app to automatically create roles if they don't exist.

Example:

java
Copy
Edit
@Bean
public CommandLineRunner initRoles(RoleRepository roleRepository) {
return args -> {
List<String> roles = List.of("ROLE_ADMIN", "ROLE_DIRECTOR", "ROLE_USER");
for (String role : roles) {
if (roleRepository.findByName(role).isEmpty()) {
roleRepository.save(new Role(role));
}
}
};
}
