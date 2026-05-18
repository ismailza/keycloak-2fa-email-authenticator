import type { SidebarsConfig } from "@docusaurus/plugin-content-docs";

const sidebars: SidebarsConfig = {
  docs: [
    "intro",
    {
      type: "category",
      label: "Installation",
      collapsed: false,
      items: ["installation/local", "installation/docker"],
    },
    {
      type: "category",
      label: "Configuration",
      collapsed: false,
      items: [
        "configuration/authentication-flow",
        "configuration/email-providers",
        "configuration/template-customization",
      ],
    },
    "local-testing",
    "contributing",
  ],
};

export default sidebars;
