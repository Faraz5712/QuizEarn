const fs = require('fs');
const path = require('path');

function getFiles(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat && stat.isDirectory()) {
      if (file !== 'node_modules' && file !== '.next' && file !== '.git') {
        results = results.concat(getFiles(fullPath));
      }
    } else {
      if (['.kt', '.ts', '.tsx', '.json', '.gradle', '.kts', '.xml'].some(ext => file.endsWith(ext))) {
        results.push(fullPath);
      }
    }
  });
  return results;
}

const files = getFiles('.');
console.log('Scanning ' + files.length + ' source files for credentials/secrets...');

const secretPatterns = [
  /AIza[0-9A-Za-z-_]{35}/, // Google API key
  /-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----/,
  /password\s*=\s*['"][^'"]{8,}['"]/i,
  /secret\s*=\s*['"][0-9a-zA-Z]{12,}['"]/i,
  /aws_secret_access_key/i,
  /ghp_[0-9a-zA-Z]{36}/
];

let leaksFound = 0;
for (const f of files) {
  const content = fs.readFileSync(f, 'utf8');
  for (const pat of secretPatterns) {
    if (pat.test(content)) {
      console.error('Potential secret leak found in ' + f + ' matching ' + pat);
      leaksFound++;
    }
  }
}

if (leaksFound === 0) {
  console.log('CREDENTIAL AUDIT PASSED: 0 hardcoded credentials or API secrets found across all ' + files.length + ' files!');
  process.exit(0);
} else {
  console.log('CREDENTIAL AUDIT FAILED with ' + leaksFound + ' findings.');
  process.exit(1);
}
