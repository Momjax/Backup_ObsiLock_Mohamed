#!/bin/bash
BASE="https://api.obsilock.iris.a3n.fr:4433"
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
PASS=0; FAIL=0

check() {
    local name="$1"; local expected="$2"; local actual="$3"
    local decoded=$(echo -e "$actual")
    if echo "$decoded" | grep -i -q "$expected"; then
        echo -e "${GREEN}✅ PASS${NC} - $name"
        PASS=$((PASS+1))
    else
        echo -e "${RED}❌ FAIL${NC} - $name"
        echo "   Expected: $expected"
        echo "   Got: $(echo "$actual" | head -c 200)"
        FAIL=$((FAIL+1))
    fi
}

echo "=========================================="
echo "🔍 AUDIT API ObsiLock FINAL - $(date)"
echo "=========================================="

# 1. AUTH
EMAIL="audit_final_$(date +%s)@obsilock.fr"
PASSWORD="AuditPassword123"
curl -sk -X POST "$BASE/auth/register" -H "Content-Type: application/json" -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" > /dev/null
R_LOGIN=$(curl -sk -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
TOKEN=$(echo "$R_LOGIN" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
AUTH="Authorization: Bearer $TOKEN"

# 2. DOSSIER & FICHIER
R_FOLD=$(curl -sk -X POST -H "$AUTH" -H "Content-Type: application/json" -d '{"name":"Audit-Folder"}' "$BASE/folders")
FOLDER_ID=$(echo "$R_FOLD" | grep -o '"id":[0-9]*' | cut -d: -f2)

echo "Fichier de test" > /tmp/test.txt
curl -sk -X POST -H "$AUTH" -F "file=@/tmp/test.txt" -F "folder_id=$FOLDER_ID" "$BASE/files" > /dev/null

# 3. PARTAGE (Le dossier n'est PAS vide ici)
echo -e "\n${YELLOW}Audit Partage Récursif...${NC}"
R_SHARE=$(curl -sk -X POST -H "$AUTH" -H "Content-Type: application/json" \
    -d "{\"kind\":\"folder\",\"target_id\":$FOLDER_ID,\"label\":\"Audit\"}" "$BASE/shares")
check "POST /shares" "token" "$R_SHARE"
TOKEN_SHARE=$(echo "$R_SHARE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN_SHARE" ]; then
    R_ZIP=$(curl -sk -L "$BASE/s/$TOKEN_SHARE/download" -w "%{http_code}")
    if [ "$R_ZIP" = "200" ]; then
        echo -e "${GREEN}✅ PASS${NC} - ZIP Partagé (HTTP 200)"
        PASS=$((PASS+1))
    else
        echo -e "${RED}❌ FAIL${NC} - ZIP Partagé (HTTP $R_ZIP)"
        FAIL=$((FAIL+1))
    fi
fi

# 4. SUPPRESSION & QUOTA
echo -e "\n${YELLOW}Audit Nettoyage...${NC}"
R_DEL=$(curl -sk -X DELETE -H "$AUTH" "$BASE/folders/$FOLDER_ID")
check "DELETE /folders (cascade)" "success\|supprimé" "$R_DEL"

echo -e "\n=========================================="
echo -e "📊 BILAN : ${GREEN}$PASS PASS${NC} / ${RED}$FAIL FAIL${NC}"
echo -e "=========================================="
