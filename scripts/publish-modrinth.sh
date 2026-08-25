#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <version-number> <jar-path>" >&2
    exit 2
fi

version_number=$1
jar_path=$2

if [[ ! -f "$jar_path" ]]; then
    echo "Artifact not found: $jar_path" >&2
    exit 2
fi

if [[ "$version_number" != *+mc* ]]; then
    echo "Version must end with +mc<Minecraft version>: $version_number" >&2
    exit 2
fi

minecraft_version=${version_number##*+mc}
modrinth_token=${MODRINTH_TOKEN:-}

if [[ -z "$modrinth_token" ]]; then
    for token_path in "$HOME/.config/blastproof/modrinth-token" /tmp/blastproof-modrinth-token; do
        if [[ -f "$token_path" ]]; then
            modrinth_token=$(tr -d '\r\n' < "$token_path")
            break
        fi
    done
fi

if [[ -z "$modrinth_token" ]]; then
    echo "MODRINTH_TOKEN is unset and no local token file was found." >&2
    exit 2
fi

existing_version_id=$(
    curl -fsSL \
        -H 'User-Agent: blastproof-release/1.0' \
        'https://api.modrinth.com/v2/project/hFyiTlND/version' \
        | node -e '
            let body = "";
            process.stdin.on("data", chunk => body += chunk);
            process.stdin.on("end", () => {
                const expected = process.argv[1];
                const version = JSON.parse(body).find(item => item.version_number === expected);
                if (version) process.stdout.write(version.id);
            });
        ' "$version_number"
)

if [[ -n "$existing_version_id" ]]; then
    echo "Modrinth version already exists: $version_number ($existing_version_id)"
    exit 0
fi

version_data=$(
    node -e '
        const versionNumber = process.argv[1];
        const minecraftVersion = process.argv[2];
        process.stdout.write(JSON.stringify({
            name: `Blastproof ${versionNumber}`,
            version_number: versionNumber,
            changelog: "Add per-source mob explosion immunity, explicit bed and respawn-anchor classification, and automated behavior coverage. Upgrading from 0.2.x: configure the explicit bed and respawn_anchor keys if you previously relied on other.",
            dependencies: [{ project_id: "P7dR8mSH", dependency_type: "required" }],
            game_versions: [minecraftVersion],
            version_type: "release",
            loaders: ["fabric"],
            featured: false,
            project_id: "hFyiTlND",
            file_parts: ["file"],
            primary_file: "file"
        }));
    ' "$version_number" "$minecraft_version"
)

response=$(
    curl -fsS -X POST \
        'https://api.modrinth.com/v2/version' \
        -H "Authorization: $modrinth_token" \
        -H 'User-Agent: blastproof-release/1.0' \
        -F "data=$version_data;type=application/json" \
        -F "file=@$jar_path;type=application/java-archive"
)

printf '%s' "$response" | node -e '
    let body = "";
    process.stdin.on("data", chunk => body += chunk);
    process.stdin.on("end", () => {
        const version = JSON.parse(body);
        console.log(`Published ${version.version_number} to Modrinth (${version.id})`);
    });
'
