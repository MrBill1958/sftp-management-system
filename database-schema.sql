package
com.nearstar.sftpmanager.model;

public
enum UserRole
{
    ADMIN("Administrator"),
    USER("User"),
    VIEWER("Viewer");

    private
final String displayName;

UserRole(String displayName) {
        this.displayName = displayName;
}

    public String getDisplayName()
    {
        return displayName;
}
}
