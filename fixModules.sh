#!/bin/bash
# Remove Spring Security imports from all Java files
find src -name "*.java" -exec sed -i '' '/import.*springframework\.security/d' {} \;

# Remove @PreAuthorize annotations
find src -name "*.java" -exec sed -i '' '/@PreAuthorize/d' {} \;

# Remove @AuthenticationPrincipal references
find src -name "*.java" -exec sed -i '' 's/@AuthenticationPrincipal UserDetails user/HttpSession session/g' {} \;