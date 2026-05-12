import csv
import re
import sys
from pathlib import Path


def parse_entries(input_path: str, output_path: str) -> None:
    """
    Reads a UTF-8 text file where entries are divided into sections.
    Section separators are lines that START with a tab character (\t).
    The tab may be followed by content, which then belongs to the NEXT section.

    Entry structure (sections separated by tab-prefixed lines):
        Section 0 : 1-2 integer lines  (entry start marker)
        Section 1 : country names      (we want the first line)
        Section 2 : logo lines
        Section 3 : match result info
        Section 4 : rating change      (+/- number)
        Section 5 : rating value       (we want the last line)
    """

    raw = Path(input_path).read_text(encoding="utf-8")
    lines = raw.splitlines()

    # --- Step 1: split lines into sections ---
    # A line that STARTS with \t is a section separator.
    # Any content after the leading tab belongs to the NEW section.
    sections: list[list[str]] = []
    current: list[str] = []
    openLines = 999
    index = -1

    for line in lines:
        openLines = openLines - 1
        index = index + 1
        if line.startswith("Home team logo") and not lines[index - 3].startswith("Home team logo") and lines[index - 3]:
            print(lines[index - 3])
            openLines = 3
        elif openLines == 0:
            # Flush the current section
            if current:
                sections.append(current)
                #print(current)
                current = []
            # Content after the tab (if any) starts the next section
            remainder = line[1:].strip()
            if remainder:
                current.append(remainder)
            openLines = 999
        else:
            stripped = line.strip()
            if stripped:
                current.append(stripped)
    exit()
    if current:  # flush final section
        sections.append(current)

    if not sections:
        print("No sections found – please check file encoding and format.")
        return

    print(f"Total sections found: {len(sections)}")

    # --- Step 2: group sections into entries ---
    # A new entry begins when ALL lines of a section are pure integers
    # (1 or 2 lines = the ranking numbers at the top of each entry).
    def is_entry_start(sec: list[str]) -> bool:
        return (
            1 <= len(sec) <= 2
            and all(re.fullmatch(r"\d+", l) for l in sec)
        )

    entries: list[list[list[str]]] = []
    current_entry: list[list[str]] = []

    for sec in sections:
        if is_entry_start(sec):
            if current_entry:
                entries.append(current_entry)
            current_entry = [sec]
        else:
            current_entry.append(sec)

    if current_entry:
        entries.append(current_entry)

    print(f"Total entries found: {len(entries)}")

    if not entries:
        print("\nFirst 10 sections for debugging:")
        for i, s in enumerate(sections[:10]):
            print(f"  Section {i}: {s}")
        return

    # --- Step 3: extract country and rating value ---
    rows: list[tuple[str, str]] = []

    for i, entry in enumerate(entries, start=1):
        if len(entry) < 2:
            print(f"Entry {i}: too few sections ({len(entry)}) – skipped.")
            continue

        # Country: first line of the second section
        country = entry[1][0] if entry[1] else ""

        # Rating: last line of the last section
        last_line = entry[-1][-1] if entry[-1] else ""

        rows.append((country, last_line))

    # --- Step 4: write CSV ---
    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["Country", "Value"])
        writer.writerows(rows)

    print(f"\n{len(rows)} entries written to '{output_path}'.")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python parse_entries.py <input.txt> <output.csv>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    if not Path(input_file).exists():
        print(f"Error: file '{input_file}' not found.")
        sys.exit(1)

    parse_entries(input_file, output_file)
