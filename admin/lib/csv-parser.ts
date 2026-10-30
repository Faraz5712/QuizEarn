import { AdminCategory, AdminQuestion, BulkImportError, BulkImportResult, DifficultyLevel, QuestionStatus } from '../types/question';

/**
 * Validates and parses raw CSV or JSON text representing bulk trivia questions.
 */
export function parseAndValidateBulkQuestions(
  rawText: string,
  categories: AdminCategory[]
): BulkImportResult {
  const trimmed = rawText.trim();
  if (!trimmed) {
    return {
      totalRows: 0,
      validQuestions: [],
      errors: [{ row: 1, field: 'payload', message: 'Input file or text is empty.' }],
    };
  }

  // Attempt JSON parsing if it begins with array bracket
  if (trimmed.startsWith('[]'.charAt(0))) {
    return parseJsonQuestions(trimmed, categories);
  }

  // Otherwise parse as standard CSV
  return parseCsvQuestions(trimmed, categories);
}

function parseJsonQuestions(
  jsonText: string,
  categories: AdminCategory[]
): BulkImportResult {
  const errors: BulkImportError[] = [];
  const validQuestions: Omit<AdminQuestion, 'id' | 'createdAt'>[] = [];

  let parsed: unknown[];
  try {
    parsed = JSON.parse(jsonText);
    if (!Array.isArray(parsed)) {
      return {
        totalRows: 0,
        validQuestions: [],
        errors: [{ row: 1, field: 'root', message: 'JSON root must be an array of question objects.' }],
      };
    }
  } catch (err: unknown) {
    return {
      totalRows: 0,
      validQuestions: [],
      errors: [
        {
          row: 1,
          field: 'json_syntax',
          message: err instanceof Error ? err.message : 'Malformed JSON syntax.',
        },
      ],
    };
  }

  parsed.forEach((item: any, idx) => {
    const rowNum = idx + 1;
    const itemErrors = validateQuestionPayload(item, rowNum, categories);
    if (itemErrors.length > 0) {
      errors.push(...itemErrors);
    } else {
      const category = categories.find(
        (c) =>
          c.id.toLowerCase() === (item.categoryId || item.category || '').toLowerCase() ||
          c.name.toLowerCase() === (item.category || item.categoryName || '').toLowerCase()
      )!;

      validQuestions.push({
        categoryId: category.id,
        categoryName: category.name,
        text: String(item.text || item.question).trim(),
        options: [
          String(item.options ? item.options[0] : item.optionA).trim(),
          String(item.options ? item.options[1] : item.optionB).trim(),
          String(item.options ? item.options[2] : item.optionC).trim(),
          String(item.options ? item.options[3] : item.optionD).trim(),
        ],
        correctOptionIndex: Number(item.correctOptionIndex),
        explanation: String(item.explanation).trim(),
        difficulty: (String(item.difficulty || 'MEDIUM').toUpperCase() as DifficultyLevel),
        timeLimitSeconds: Number(item.timeLimitSeconds) || 15,
        pointsReward: Number(item.pointsReward) || 10,
        status: (String(item.status || 'ACTIVE').toUpperCase() as QuestionStatus),
      });
    }
  });

  return {
    totalRows: parsed.length,
    validQuestions,
    errors,
  };
}

function parseCsvQuestions(
  csvText: string,
  categories: AdminCategory[]
): BulkImportResult {
  const lines = csvText.split(/\r?\n/).filter((line) => line.trim().length > 0);
  if (lines.length < 2) {
    return {
      totalRows: lines.length,
      validQuestions: [],
      errors: [{ row: 1, field: 'csv', message: 'CSV requires a header line and at least one data row.' }],
    };
  }

  const headerLine = lines[0];
  const headers = splitCsvRow(headerLine).map((h) => h.trim().toLowerCase());

  const errors: BulkImportError[] = [];
  const validQuestions: Omit<AdminQuestion, 'id' | 'createdAt'>[] = [];

  for (let i = 1; i < lines.length; i++) {
    const rowNum = i + 1; // 1-based index including header
    const values = splitCsvRow(lines[i]);

    if (values.length < 9) {
      errors.push({
        row: rowNum,
        field: 'columns',
        message: `Row has ${values.length} columns, expected at least 9.`,
      });
      continue;
    }

    const rowObj: Record<string, string> = {};
    headers.forEach((h, colIdx) => {
      rowObj[h] = values[colIdx] ? values[colIdx].trim() : '';
    });

    const itemPayload = {
      category: rowObj['category'] || rowObj['categoryid'] || rowObj['category_id'],
      difficulty: rowObj['difficulty'],
      question: rowObj['question'] || rowObj['text'],
      optionA: rowObj['optiona'] || rowObj['option_a'] || rowObj['option1'],
      optionB: rowObj['optionb'] || rowObj['option_b'] || rowObj['option2'],
      optionC: rowObj['optionc'] || rowObj['option_c'] || rowObj['option3'],
      optionD: rowObj['optiond'] || rowObj['option_d'] || rowObj['option4'],
      correctOptionIndex: rowObj['correctoptionindex'] || rowObj['correct_option_index'] || rowObj['correct_index'] || rowObj['answer'],
      explanation: rowObj['explanation'],
      timeLimitSeconds: rowObj['timelimitseconds'] || rowObj['time_limit'] || '15',
      pointsReward: rowObj['pointsreward'] || rowObj['points'] || '10',
      status: rowObj['status'] || 'ACTIVE',
    };

    const itemErrors = validateQuestionPayload(itemPayload, rowNum, categories);
    if (itemErrors.length > 0) {
      errors.push(...itemErrors);
    } else {
      const category = categories.find(
        (c) =>
          c.id.toLowerCase() === (itemPayload.category || '').toLowerCase() ||
          c.name.toLowerCase() === (itemPayload.category || '').toLowerCase()
      )!;

      validQuestions.push({
        categoryId: category.id,
        categoryName: category.name,
        text: itemPayload.question,
        options: [
          itemPayload.optionA,
          itemPayload.optionB,
          itemPayload.optionC,
          itemPayload.optionD,
        ],
        correctOptionIndex: Number(itemPayload.correctOptionIndex),
        explanation: itemPayload.explanation,
        difficulty: (itemPayload.difficulty.toUpperCase() as DifficultyLevel),
        timeLimitSeconds: Number(itemPayload.timeLimitSeconds) || 15,
        pointsReward: Number(itemPayload.pointsReward) || 10,
        status: (itemPayload.status.toUpperCase() as QuestionStatus),
      });
    }
  }

  return {
    totalRows: lines.length - 1,
    validQuestions,
    errors,
  };
}

function validateQuestionPayload(
  item: any,
  rowNum: number,
  categories: AdminCategory[]
): BulkImportError[] {
  const errs: BulkImportError[] = [];

  // 1. Category validation
  const catVal = (item.categoryId || item.category || item.categoryName || '').trim();
  if (!catVal) {
    errs.push({ row: rowNum, field: 'category', message: 'Category identifier or name is required.' });
  } else {
    const matched = categories.find(
      (c) => c.id.toLowerCase() === catVal.toLowerCase() || c.name.toLowerCase() === catVal.toLowerCase()
    );
    if (!matched) {
      errs.push({
        row: rowNum,
        field: 'category',
        message: `Unknown category "${catVal}". Valid: ${categories.map((c) => c.name).join(', ')}.`,
      });
    }
  }

  // 2. Question Text
  const questionText = (item.text || item.question || '').trim();
  if (!questionText) {
    errs.push({ row: rowNum, field: 'question', message: 'Question text cannot be blank.' });
  } else if (questionText.length < 8) {
    errs.push({ row: rowNum, field: 'question', message: 'Question text must be at least 8 characters.' });
  }

  // 3. Options validation
  const optA = (item.options ? item.options[0] : item.optionA || '').trim();
  const optB = (item.options ? item.options[1] : item.optionB || '').trim();
  const optC = (item.options ? item.options[2] : item.optionC || '').trim();
  const optD = (item.options ? item.options[3] : item.optionD || '').trim();

  if (!optA || !optB || !optC || !optD) {
    errs.push({ row: rowNum, field: 'options', message: 'All 4 options (A, B, C, D) are required.' });
  } else {
    const unique = new Set([optA, optB, optC, optD]);
    if (unique.size < 4) {
      errs.push({ row: rowNum, field: 'options', message: 'All 4 options must be distinct values.' });
    }
  }

  // 4. Correct Option Index
  const ansVal = item.correctOptionIndex ?? item.correct_index ?? item.answer;
  if (ansVal === undefined || ansVal === '') {
    errs.push({ row: rowNum, field: 'correctOptionIndex', message: 'Correct option index (0, 1, 2, or 3) is required.' });
  } else {
    const num = Number(ansVal);
    if (isNaN(num) || num < 0 || num > 3 || !Number.isInteger(num)) {
      errs.push({
        row: rowNum,
        field: 'correctOptionIndex',
        message: `Invalid correct option index "${ansVal}". Must be 0, 1, 2, or 3.`,
      });
    }
  }

  // 5. Explanation
  const explanation = (item.explanation || '').trim();
  if (!explanation) {
    errs.push({ row: rowNum, field: 'explanation', message: 'Educational explanation is required.' });
  } else if (explanation.length < 10) {
    errs.push({ row: rowNum, field: 'explanation', message: 'Explanation must be at least 10 characters.' });
  }

  // 6. Difficulty
  const diff = (item.difficulty || '').toUpperCase().trim();
  if (diff && !['EASY', 'MEDIUM', 'HARD'].includes(diff)) {
    errs.push({
      row: rowNum,
      field: 'difficulty',
      message: `Invalid difficulty "${diff}". Valid: EASY, MEDIUM, HARD.`,
    });
  }

  return errs;
}

/**
 * Handles CSV quoted fields with embedded commas.
 */
function splitCsvRow(rowText: string): string[] {
  const result: string[] = [];
  let current = '';
  let inQuotes = false;

  for (let i = 0; i < rowText.length; i++) {
    const char = rowText[i];
    if (char === '"') {
      if (inQuotes && rowText[i + 1] === '"') {
        current += '"';
        i++; // skip escaped quote
      } else {
        inQuotes = !inQuotes;
      }
    } else if (char === ',' && !inQuotes) {
      result.push(current);
      current = '';
    } else {
      current += char;
    }
  }
  result.push(current);
  return result;
}

export const SAMPLE_CSV_TEMPLATE = `category,difficulty,question,optionA,optionB,optionC,optionD,correctOptionIndex,explanation,timeLimitSeconds,pointsReward,status
Science & Nature,MEDIUM,What is the chemical symbol for the element Tungsten?,W,Tu,Tg,Tn,0,Tungsten is symbolized as W from its German mineral name Wolfram.,15,10,ACTIVE
Technology & Computing,EASY,Which company originally developed the TypeScript programming language?,Google,Microsoft,Apple,Facebook,1,Anders Hejlsberg and Microsoft released TypeScript v0.8 in October 2012.,15,10,ACTIVE
World History,HARD,Which Roman Emperor constructed the wall across northern Britannia in 122 AD?,Nero,Hadrian,Trajan,Augustus,1,Emperor Hadrian ordered the construction of Hadrian's Wall across northern England in 122 AD.,20,15,ACTIVE
Geography & Capitals,EASY,What is the capital city of Japan?,Kyoto,Osaka,Tokyo,Hiroshima,2,Tokyo has served as the capital and imperial seat of Japan since 1868.,15,10,ACTIVE`;

export const SAMPLE_JSON_TEMPLATE = `[
  {
    "category": "Science & Nature",
    "difficulty": "MEDIUM",
    "question": "What is the chemical symbol for the element Tungsten?",
    "options": ["W", "Tu", "Tg", "Tn"],
    "correctOptionIndex": 0,
    "explanation": "Tungsten is symbolized as W from its German mineral name Wolfram.",
    "timeLimitSeconds": 15,
    "pointsReward": 10,
    "status": "ACTIVE"
  },
  {
    "category": "Technology & Computing",
    "difficulty": "EASY",
    "question": "Which company originally developed the TypeScript programming language?",
    "options": ["Google", "Microsoft", "Apple", "Facebook"],
    "correctOptionIndex": 1,
    "explanation": "Anders Hejlsberg and Microsoft released TypeScript v0.8 in October 2012.",
    "timeLimitSeconds": 15,
    "pointsReward": 10,
    "status": "ACTIVE"
  }
]`;
