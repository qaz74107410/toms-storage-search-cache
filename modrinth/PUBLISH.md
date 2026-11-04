# Publishing the mod to Modrinth — runbook

Prep done. The remaining steps need your GitHub/Modrinth account and a machine that can build the jar
(the build script compiles against a local Forge install, so this runs on the Mac, not the Pi).

## 1. Build the jar (on the Mac)

```bash
git clone https://github.com/qaz74107410/toms-storage-search-cache
cd toms-storage-search-cache

# LIBS = a Forge 1.20.1-47.4.13 install's libraries dir (PrismLauncher keeps one)
# MODS = a mods folder containing Tom's Simple Storage, EMI and Cloth Config jars
LIBS="$HOME/Library/Application Support/PrismLauncher/libraries" \
MODS="$HOME/Library/Application Support/PrismLauncher/instances/<instance>/minecraft/mods" \
./build.sh
```

Output: `build/TomsStorageSearchCache-1.20.1-1.1.0.jar`. Requires JDK 17+.

Sanity check before uploading: drop the jar into an instance with Tom's 1.7.1, type in a terminal search,
confirm no freeze and that search results are unchanged.

## 2. Create the project

Web UI: <https://modrinth.com/projects> → **Create a project** → **Mod**.

```bash
# or via API — requires a Modrinth PAT with the `create project` scope
export MODRINTH_TOKEN=mrp_...
curl -X POST https://api.modrinth.com/v2/project \
  -H "Authorization: $MODRINTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "toms-storage-search-cache",
    "title": "Tom'"'"'s Storage Search Cache Fix",
    "description": "Stops the per-keystroke freeze when searching Tom'"'"'s Simple Storage terminals. Client-side mixin that lengthens the tooltip cache instead of changing search results.",
    "categories": ["optimization", "storage", "utility"],
    "client_side": "required",
    "server_side": "unsupported",
    "body": "<paste the Body section of listing.md>",
    "license_id": "Apache-2.0",
    "source_url": "https://github.com/qaz74107410/toms-storage-search-cache",
    "issues_url": "https://github.com/qaz74107410/toms-storage-search-cache/issues"
  }'
```

Then upload `icon.png` (512×512) in the project settings.

## 3. Upload the version

```bash
export PROJECT_ID=<id returned above>
curl -X POST https://api.modrinth.com/v2/version \
  -H "Authorization: $MODRINTH_TOKEN" \
  -F "data={\"name\":\"1.1.0 for 1.20.1\",\"version_number\":\"1.1.0+1.20.1\",\"changelog\":\"First public release: tooltip cache lifetime fix for terminal search, EMI snapshot skipping inside terminals, Cloth Config options.\",\"dependencies\":[{\"project_id\":\"XZNI4Cpy\",\"dependency_type\":\"required\"}],\"game_versions\":[\"1.20.1\"],\"version_type\":\"release\",\"loaders\":[\"forge\"],\"featured\":false};type=json" \
  -F "file=@build/TomsStorageSearchCache-1.20.1-1.1.0.jar"
```

## 4. After it is live

- Add the Modrinth badge + download count to the mod's GitHub README.
- Then the profile card can show a real number instead of `0`: Modrinth's API is public —
  `https://api.modrinth.com/v2/project/<slug>` returns `downloads`, so `gen_projects.py` can read it.

## Notes / guardrails

- Modrinth requires the project to be **your own work** and to state clearly that it is an addon, not a
  fork of Tom's. The body and license line already say so.
- Do not list it as supporting 1.20.2+ until it has actually been tested there; a wrong version claim is
  the top cause of bug reports on Modrinth.
- Optional dependencies (EMI, Cloth Config) are referenced in the body only — they load fine when absent.
