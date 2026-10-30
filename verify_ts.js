const fs = require('fs');
const path = require('path');

function getFiles(dir, exts = ['.ts', '.tsx']) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat && stat.isDirectory()) {
      if (file !== 'node_modules' && file !== '.next') {
        results = results.concat(getFiles(fullPath, exts));
      }
    } else {
      if (exts.some(ext => file.endsWith(ext))) {
        results.push(fullPath);
      }
    }
  });
  return results;
}

const files = getFiles('./admin');
console.log('Total TS/TSX files scanned:', files.length);

let errors = 0;
for (const file of files) {
  const content = fs.readFileSync(file, 'utf8');
  let parens = 0, braces = 0, brackets = 0;
  let inString = false, stringChar = '';
  let inLineComment = false, inBlockComment = false;

  for (let i = 0; i < content.length; i++) {
    const char = content[i];
    const prev = i > 0 ? content[i - 1] : '';
    const next = i < content.length - 1 ? content[i + 1] : '';

    if (inLineComment) {
      if (char === '\n') inLineComment = false;
      continue;
    }
    if (inBlockComment) {
      if (char === '*' && next === '/') {
        inBlockComment = false;
        i++;
      }
      continue;
    }
    if (inString) {
      if (char === stringChar && prev !== '\\') {
        inString = false;
      }
      continue;
    }

    if (char === '/' && next === '/') {
      inLineComment = true;
      i++;
      continue;
    }
    if (char === '/' && next === '*') {
      inBlockComment = true;
      i++;
      continue;
    }
    if (char === '"' || char === "'" || char === '`') {
      inString = true;
      stringChar = char;
      continue;
    }

    if (char === '(') parens++;
    else if (char === ')') parens--;
    else if (char === '{') braces++;
    else if (char === '}') braces--;
    else if (char === '[') brackets++;
    else if (char === ']') brackets--;

    if (parens < 0 || braces < 0 || brackets < 0) {
      console.error('Negative nesting in ' + file + ' at char ' + i + ': ' + char);
      errors++;
      break;
    }
  }

  if (parens !== 0 || braces !== 0 || brackets !== 0) {
    console.error('Mismatch in ' + file + ': parens=' + parens + ', braces=' + braces + ', brackets=' + brackets);
    errors++;
  }
}

if (errors === 0) {
  console.log('ALL ' + files.length + ' TS/TSX files passed syntax balance verification perfectly!');
  process.exit(0);
} else {
  console.log('Errors found: ' + errors);
  process.exit(1);
}
