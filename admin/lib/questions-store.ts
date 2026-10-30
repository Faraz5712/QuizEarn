'use client';

import { AdminCategory, AdminQuestion, DifficultyLevel, QuestionStatus } from '../types/question';

export const INITIAL_CATEGORIES: AdminCategory[] = [
  {
    id: 'cat_science',
    name: 'Science & Nature',
    slug: 'science-nature',
    iconName: 'spark',
    colorHex: '#2DD4BF',
    displayOrder: 1,
    isActive: true,
    questionCount: 5,
    description: 'Physics, biology, chemistry, and astronomical discoveries.',
  },
  {
    id: 'cat_tech',
    name: 'Technology & Computing',
    slug: 'technology',
    iconName: 'quiz',
    colorHex: '#5B5FEF',
    displayOrder: 2,
    isActive: true,
    questionCount: 5,
    description: 'Software development, hardware, internet history, and artificial intelligence.',
  },
  {
    id: 'cat_history',
    name: 'World History',
    slug: 'history',
    iconName: 'shield',
    colorHex: '#F59E0B',
    displayOrder: 3,
    isActive: true,
    questionCount: 4,
    description: 'Ancient civilizations, world revolutions, treaties, and historical figures.',
  },
  {
    id: 'cat_geography',
    name: 'Geography & Capitals',
    slug: 'geography',
    iconName: 'trophy',
    colorHex: '#8B5CF6',
    displayOrder: 4,
    isActive: true,
    questionCount: 4,
    description: 'Global capitals, geographic landmarks, rivers, and mountain ranges.',
  },
  {
    id: 'cat_pop_culture',
    name: 'Pop Culture & Arts',
    slug: 'pop-culture',
    iconName: 'gift',
    colorHex: '#EC4899',
    displayOrder: 5,
    isActive: true,
    questionCount: 4,
    description: 'Literature, cinema, music history, and modern artistic movements.',
  },
  {
    id: 'cat_sports',
    name: 'Sports & Athletics',
    slug: 'sports',
    iconName: 'users',
    colorHex: '#10B981',
    displayOrder: 6,
    isActive: true,
    questionCount: 3,
    description: 'Olympics, world championships, athletics records, and team sports.',
  },
];

export const INITIAL_QUESTIONS: AdminQuestion[] = [
  // Science & Nature
  {
    id: 'QZ-SCI-001',
    categoryId: 'cat_science',
    categoryName: 'Science & Nature',
    text: 'What is the primary gas composing the atmosphere of Venus?',
    options: ['Nitrogen', 'Carbon Dioxide', 'Oxygen', 'Methane'],
    correctOptionIndex: 1,
    explanation: 'Venus has an extremely dense atmosphere composed of approximately 96.5% Carbon Dioxide, causing an intense runaway greenhouse effect.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-20T10:15:00Z',
  },
  {
    id: 'QZ-SCI-002',
    categoryId: 'cat_science',
    categoryName: 'Science & Nature',
    text: 'Which human organ consumes roughly 20% of the body’s resting metabolic energy?',
    options: ['Liver', 'Heart', 'Brain', 'Kidneys'],
    correctOptionIndex: 2,
    explanation: 'Despite representing only 2% of total body weight, the human brain consumes about 20% of the body’s total oxygen and caloric energy.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-21T09:30:00Z',
  },
  {
    id: 'QZ-SCI-003',
    categoryId: 'cat_science',
    categoryName: 'Science & Nature',
    text: 'What subatomic particle was definitively discovered at the CERN Large Hadron Collider in 2012?',
    options: ['Higgs Boson', 'Top Quark', 'Tau Neutrino', 'Graviton'],
    correctOptionIndex: 0,
    explanation: 'The Higgs Boson particle, essential to the Standard Model of particle physics for explaining mass, was observed at CERN in July 2012.',
    difficulty: 'HARD',
    timeLimitSeconds: 20,
    pointsReward: 15,
    status: 'ACTIVE',
    createdAt: '2026-10-22T14:45:00Z',
  },
  {
    id: 'QZ-SCI-004',
    categoryId: 'cat_science',
    categoryName: 'Science & Nature',
    text: 'What is the chemical element with the atomic number 79?',
    options: ['Silver', 'Gold', 'Platinum', 'Mercury'],
    correctOptionIndex: 1,
    explanation: 'Gold has the atomic number 79 and chemical symbol Au (derived from the Latin word "Aurum").',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-23T11:20:00Z',
  },
  {
    id: 'QZ-SCI-005',
    categoryId: 'cat_science',
    categoryName: 'Science & Nature',
    text: 'What type of bond forms when electrons are shared between nonmetal atoms?',
    options: ['Ionic Bond', 'Covalent Bond', 'Hydrogen Bond', 'Metallic Bond'],
    correctOptionIndex: 1,
    explanation: 'Covalent chemical bonds form through the cooperative sharing of valence electrons between atoms.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'DRAFT',
    createdAt: '2026-10-28T16:00:00Z',
  },

  // Technology & Computing
  {
    id: 'QZ-TEC-001',
    categoryId: 'cat_tech',
    categoryName: 'Technology & Computing',
    text: 'Which programming language originally introduced coroutines in 1958 (Melvin Conway)?',
    options: ['Simula', 'LISP', 'Assembly Language', 'FORTRAN'],
    correctOptionIndex: 2,
    explanation: 'Melvin Conway coined the term "coroutine" in 1958 while implementing an assembly language compiler.',
    difficulty: 'HARD',
    timeLimitSeconds: 20,
    pointsReward: 15,
    status: 'ACTIVE',
    createdAt: '2026-10-20T11:00:00Z',
  },
  {
    id: 'QZ-TEC-002',
    categoryId: 'cat_tech',
    categoryName: 'Technology & Computing',
    text: 'What does the acronym REST stand for in modern web API design?',
    options: [
      'Representational State Transfer',
      'Remote Execution Service Terminal',
      'Recursive State Tree',
      'Relational Entity Standard Transmission',
    ],
    correctOptionIndex: 0,
    explanation: 'REST stands for Representational State Transfer, introduced by Roy Fielding in his 2000 doctoral dissertation.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-21T13:10:00Z',
  },
  {
    id: 'QZ-TEC-003',
    categoryId: 'cat_tech',
    categoryName: 'Technology & Computing',
    text: 'In relational databases, which ACID property ensures transactions are all-or-nothing?',
    options: ['Consistency', 'Atomicity', 'Isolation', 'Durability'],
    correctOptionIndex: 1,
    explanation: 'Atomicity guarantees that each transaction is treated as a single indivisible unit, completely committing or rolling back.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-22T08:50:00Z',
  },
  {
    id: 'QZ-TEC-004',
    categoryId: 'cat_tech',
    categoryName: 'Technology & Computing',
    text: 'Which cryptographic algorithm forms the basis of TLS 1.3 key exchange?',
    options: ['ECDHE', 'DES', 'MD5', 'SHA-1'],
    correctOptionIndex: 0,
    explanation: 'Elliptic-Curve Diffie-Hellman Ephemeral (ECDHE) provides forward secrecy for secure key exchange in TLS 1.3.',
    difficulty: 'HARD',
    timeLimitSeconds: 20,
    pointsReward: 15,
    status: 'ACTIVE',
    createdAt: '2026-10-24T15:25:00Z',
  },
  {
    id: 'QZ-TEC-005',
    categoryId: 'cat_tech',
    categoryName: 'Technology & Computing',
    text: 'What year was the Android operating system officially unveiled by the Open Handset Alliance?',
    options: ['2005', '2007', '2008', '2010'],
    correctOptionIndex: 1,
    explanation: 'The Open Handset Alliance officially announced Android on November 5, 2007.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-25T17:30:00Z',
  },

  // World History
  {
    id: 'QZ-HIS-001',
    categoryId: 'cat_history',
    categoryName: 'World History',
    text: 'In which year did the Apollo 11 mission achieve humanity’s first crewed lunar landing?',
    options: ['1967', '1969', '1971', '1973'],
    correctOptionIndex: 1,
    explanation: 'Apollo 11 landed astronauts Neil Armstrong and Buzz Aldrin on the Moon on July 20, 1969.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-21T10:00:00Z',
  },
  {
    id: 'QZ-HIS-002',
    categoryId: 'cat_history',
    categoryName: 'World History',
    text: 'Which ancient library of the Hellenistic world was founded in Ptolemaic Egypt?',
    options: ['Library of Alexandria', 'Library of Pergamum', 'Library of Ashurbanipal', 'House of Wisdom'],
    correctOptionIndex: 0,
    explanation: 'The Great Library of Alexandria in Egypt was one of the largest and most significant scholarly libraries of antiquity.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-22T16:20:00Z',
  },
  {
    id: 'QZ-HIS-003',
    categoryId: 'cat_history',
    categoryName: 'World History',
    text: 'The Magna Carta was signed by King John of England at Runnymede in which year?',
    options: ['1066', '1215', '1415', '1588'],
    correctOptionIndex: 1,
    explanation: 'King John granted the Magna Carta on June 15, 1215, establishing the principle that everyone is subject to the rule of law.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-23T14:10:00Z',
  },
  {
    id: 'QZ-HIS-004',
    categoryId: 'cat_history',
    categoryName: 'World History',
    text: 'Which peace agreement in 1648 ended the Thirty Years’ War in Europe?',
    options: ['Treaty of Versailles', 'Peace of Westphalia', 'Treaty of Utrecht', 'Congress of Vienna'],
    correctOptionIndex: 1,
    explanation: 'The Peace of Westphalia established modern state sovereignty and diplomatic relations in 1648.',
    difficulty: 'HARD',
    timeLimitSeconds: 20,
    pointsReward: 15,
    status: 'ACTIVE',
    createdAt: '2026-10-24T18:00:00Z',
  },

  // Geography & Capitals
  {
    id: 'QZ-GEO-001',
    categoryId: 'cat_geography',
    categoryName: 'Geography & Capitals',
    text: 'What is the official capital city of Australia?',
    options: ['Sydney', 'Melbourne', 'Canberra', 'Brisbane'],
    correctOptionIndex: 2,
    explanation: 'Canberra was chosen as the compromise capital of Australia in 1908, situated between Sydney and Melbourne.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-21T12:00:00Z',
  },
  {
    id: 'QZ-GEO-002',
    categoryId: 'cat_geography',
    categoryName: 'Geography & Capitals',
    text: 'Which body of water is the lowest land point on Earth’s surface?',
    options: ['Lake Baikal', 'Dead Sea', 'Caspian Sea', 'Lake Tanganyika'],
    correctOptionIndex: 1,
    explanation: 'The Dead Sea shores sit at approximately 430 meters (1,410 ft) below sea level, the lowest dry land on Earth.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-22T15:40:00Z',
  },
  {
    id: 'QZ-GEO-003',
    categoryId: 'cat_geography',
    categoryName: 'Geography & Capitals',
    text: 'How many countries directly border the landlocked Caspian Sea?',
    options: ['3', '4', '5', '6'],
    correctOptionIndex: 2,
    explanation: 'Five countries border the Caspian Sea: Russia, Kazakhstan, Turkmenistan, Iran, and Azerbaijan.',
    difficulty: 'HARD',
    timeLimitSeconds: 20,
    pointsReward: 15,
    status: 'ACTIVE',
    createdAt: '2026-10-23T17:15:00Z',
  },
  {
    id: 'QZ-GEO-004',
    categoryId: 'cat_geography',
    categoryName: 'Geography & Capitals',
    text: 'What is the highest unbroken waterfall in the world?',
    options: ['Niagara Falls', 'Victoria Falls', 'Angel Falls', 'Iguazu Falls'],
    correctOptionIndex: 2,
    explanation: 'Angel Falls in Canaima National Park, Venezuela drops an astounding 979 meters (3,212 feet).',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-24T19:00:00Z',
  },

  // Pop Culture & Arts
  {
    id: 'QZ-POP-001',
    categoryId: 'cat_pop_culture',
    categoryName: 'Pop Culture & Arts',
    text: 'Who painted the famous post-impressionist masterpiece "The Starry Night"?',
    options: ['Claude Monet', 'Vincent van Gogh', 'Pablo Picasso', 'Salvador Dalí'],
    correctOptionIndex: 1,
    explanation: 'Vincent van Gogh painted "The Starry Night" in June 1889 during his stay at the Saint-Paul-de-Mausole asylum in Saint-Rémy-de-Provence.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-20T14:00:00Z',
  },
  {
    id: 'QZ-POP-002',
    categoryId: 'cat_pop_culture',
    categoryName: 'Pop Culture & Arts',
    text: 'Which film won the Academy Award for Best Picture in 2020, becoming the first non-English language film to do so?',
    options: ['Roma', 'Parasite', 'Amour', 'Crouching Tiger, Hidden Dragon'],
    correctOptionIndex: 1,
    explanation: 'Bong Joon-ho’s South Korean masterpiece "Parasite" won Best Picture at the 92nd Academy Awards in 2020.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-22T20:30:00Z',
  },
  {
    id: 'QZ-POP-003',
    categoryId: 'cat_pop_culture',
    categoryName: 'Pop Culture & Arts',
    text: 'What epic fantasy novel series features the kingdom of Westeros and the Iron Throne?',
    options: ['The Wheel of Time', 'A Song of Ice and Fire', 'The Stormlight Archive', 'The Witcher'],
    correctOptionIndex: 1,
    explanation: 'George R. R. Martin authored "A Song of Ice and Fire", famously adapted for television as "Game of Thrones".',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-23T21:00:00Z',
  },
  {
    id: 'QZ-POP-004',
    categoryId: 'cat_pop_culture',
    categoryName: 'Pop Culture & Arts',
    text: 'Which architectural style is characterized by pointed arches, ribbed vaults, and flying buttresses?',
    options: ['Baroque', 'Gothic', 'Romanesque', 'Neoclassical'],
    correctOptionIndex: 1,
    explanation: 'Gothic architecture originated in 12th-century France and is celebrated for structures like Notre-Dame de Paris.',
    difficulty: 'MEDIUM',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'DRAFT',
    createdAt: '2026-10-25T11:45:00Z',
  },

  // Sports & Athletics
  {
    id: 'QZ-SPO-001',
    categoryId: 'cat_sports',
    categoryName: 'Sports & Athletics',
    text: 'How many minutes is a standard regulation FIFA World Cup soccer match (excluding extra time)?',
    options: ['80 minutes', '90 minutes', '100 minutes', '120 minutes'],
    correctOptionIndex: 1,
    explanation: 'A regulation soccer match consists of two 45-minute halves for a total of 90 minutes.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-21T08:00:00Z',
  },
  {
    id: 'QZ-SPO-002',
    categoryId: 'cat_sports',
    categoryName: 'Sports & Athletics',
    text: 'Which athlete holds the men’s world record for the 100-meter sprint at 9.58 seconds?',
    options: ['Tyson Gay', 'Yohan Blake', 'Usain Bolt', 'Carl Lewis'],
    correctOptionIndex: 2,
    explanation: 'Usain Bolt of Jamaica set the world record of 9.58 seconds at the 2009 World Athletics Championships in Berlin.',
    difficulty: 'EASY',
    timeLimitSeconds: 15,
    pointsReward: 10,
    status: 'ACTIVE',
    createdAt: '2026-10-22T19:15:00Z',
  },
  {
    id: 'QZ-SPO-003',
    categoryId: 'cat_sports',
    categoryName: 'Sports & Athletics',
    text: 'In tennis, winning all four Grand Slam tournaments plus the Olympic Gold medal in a single calendar year is called what?',
    options: ['Grand Slam', 'Career Slam', 'Golden Slam', 'Super Slam'],
    correctOptionIndex: 2,
    explanation: 'Steffi Graf achieved the historic Golden Slam in 1988, winning the Australian, French, Wimbledon, and US Opens plus Seoul Olympic Gold.',
    difficulty: 'HARD',
    timeLimitSeconds: 20,
    pointsReward: 15,
    status: 'ACTIVE',
    createdAt: '2026-10-23T22:00:00Z',
  },
];

const QUESTIONS_KEY = 'quizearn_admin_questions';
const CATEGORIES_KEY = 'quizearn_admin_categories';

export function loadStoredQuestions(): AdminQuestion[] {
  if (typeof window === 'undefined') return INITIAL_QUESTIONS;
  try {
    const raw = localStorage.getItem(QUESTIONS_KEY);
    if (!raw) {
      localStorage.setItem(QUESTIONS_KEY, JSON.stringify(INITIAL_QUESTIONS));
      return INITIAL_QUESTIONS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_QUESTIONS;
  }
}

export function saveStoredQuestions(questions: AdminQuestion[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(QUESTIONS_KEY, JSON.stringify(questions));
  } catch (err) {
    console.error('Failed to persist questions to localStorage', err);
  }
}

export function loadStoredCategories(): AdminCategory[] {
  if (typeof window === 'undefined') return INITIAL_CATEGORIES;
  try {
    const raw = localStorage.getItem(CATEGORIES_KEY);
    if (!raw) {
      localStorage.setItem(CATEGORIES_KEY, JSON.stringify(INITIAL_CATEGORIES));
      return INITIAL_CATEGORIES;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_CATEGORIES;
  }
}

export function saveStoredCategories(categories: AdminCategory[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(CATEGORIES_KEY, JSON.stringify(categories));
  } catch (err) {
    console.error('Failed to persist categories to localStorage', err);
  }
}
