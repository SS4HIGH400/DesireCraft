var clanTag = "%desire_clan_tag%";

function formatClanLine() {
  if (!clanTag || clanTag.trim() === "" || clanTag.indexOf("%") !== -1) {
    return "";
  }

  return "&8 ⋅ &fГильдия &8⇾ &7" + clanTag;
}

formatClanLine();
