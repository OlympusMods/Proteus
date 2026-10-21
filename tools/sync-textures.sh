#!/bin/sh
set -e
dir=common/resources/assets/proteus/textures
src=${1:-$(git rev-parse --abbrev-ref HEAD)}
tree=$(git rev-parse "$src:$dir")
for branch in 1.20 1.21 26.1 main; do
    [ "$branch" = "$src" ] && continue
    git rev-parse --verify -q "refs/heads/$branch" >/dev/null || continue
    [ "$(git rev-parse "$branch:$dir" 2>/dev/null)" = "$tree" ] && continue
    index=$(mktemp)
    rm -f "$index"
    GIT_INDEX_FILE=$index git read-tree "$branch"
    GIT_INDEX_FILE=$index git rm --cached -f -r -q --ignore-unmatch "$dir"
    GIT_INDEX_FILE=$index git read-tree --prefix="$dir/" "$tree"
    new=$(GIT_INDEX_FILE=$index git write-tree)
    rm -f "$index"
    old=$(git rev-parse "refs/heads/$branch")
    commit=$(git commit-tree "$new" -p "$old" -m "chore: sync textures from $src")
    git update-ref -m "sync textures from $src" "refs/heads/$branch" "$commit" "$old"
    echo "synced textures to $branch"
done
