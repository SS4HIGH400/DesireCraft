var clanTag = "%desire_clan_tag%";

function formatClanTag() {
  if (!clanTag || clanTag.trim() === "" || clanTag.indexOf("%") !== -1) {
    return "";
  }

  return clanTag;
}

formatClanTag();
