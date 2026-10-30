import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./lib/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        brand: {
          primary: "#5B5FEF",
          secondary: "#8B5CF6",
          mint: "#2DD4BF",
          gold: "#F5B942",
          crimson: "#EF4444",
          amber: "#F59E0B",
          dark: {
            bg: "#10111A",
            surface: "#1A1C28",
            card: "#24283B",
            border: "#323854",
          },
          light: {
            bg: "#F7F8FC",
            surface: "#FFFFFF",
            card: "#EEF0F8",
            border: "#E2E4F0",
          },
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "sans-serif"],
      },
    },
  },
  plugins: [],
};

export default config;
