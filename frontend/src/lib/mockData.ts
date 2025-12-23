export interface Campaign {
  id: string;
  title: string;
  ngoName: string;
  organizationId: string;
  organizationName: string;
  organizationLogo?: string;
  transparencyScore?: number;
  description: string;
  detailedDescription: string;
  image: string;
  category: string;
  country: string;
  targetAmount: number;
  currentAmount: number;
  startDate: string;
  endDate: string;
  status: "active" | "completed" | "pending";
}

export interface NGO {
  id: string;
  name: string;
  description: string;
  logo: string;
  verified: boolean;
}

export interface CartItem {
  campaignId: string;
  amount: number;
}

export const mockNGOs: NGO[] = [
  {
    id: "1",
    name: "International Relief Fund",
    description: "Providing emergency aid and long-term development programs worldwide",
    logo: "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=200&h=200&fit=crop",
    verified: true,
  },
  {
    id: "2",
    name: "Hope for Children Foundation",
    description: "Supporting education and healthcare for children in need",
    logo: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=200&h=200&fit=crop",
    verified: true,
  },
  {
    id: "3",
    name: "Clean Water Initiative",
    description: "Building sustainable water infrastructure in developing regions",
    logo: "https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=200&h=200&fit=crop",
    verified: true,
  },
];

export const mockCampaigns: Campaign[] = [
  {
    id: "1",
    title: "Emergency Food Relief for Gaza",
    ngoName: "International Relief Fund",
    organizationId: "1",
    organizationName: "International Relief Fund",
    organizationLogo: "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=200&h=200&fit=crop",
    transparencyScore: 94,
    description: "Providing essential food supplies to families affected by the humanitarian crisis",
    detailedDescription: `This emergency campaign aims to provide immediate food relief to families in Gaza who are facing severe food shortages. Your donation will help us purchase and distribute food packages containing rice, flour, cooking oil, canned goods, and other essential items.

Each food package costs approximately $50 and can feed a family of 5 for two weeks. We are working with local partners on the ground to ensure safe and efficient distribution directly to families in need.

The situation is urgent, and every contribution makes a difference. All donations will be used exclusively for purchasing and distributing food supplies. We provide full transparency with regular updates and receipts for all purchases.`,
    image: "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=800&h=600&fit=crop",
    category: "Food Aid",
    country: "Palestine",
    targetAmount: 100000,
    currentAmount: 67500,
    startDate: "2024-01-15",
    endDate: "2024-06-30",
    status: "active",
  },
  {
    id: "2",
    title: "Build a School in Rural Africa",
    ngoName: "Hope for Children Foundation",
    organizationId: "2",
    organizationName: "Hope for Children Foundation",
    organizationLogo: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=200&h=200&fit=crop",
    transparencyScore: 87,
    description: "Constructing a primary school to provide education for 500 children",
    detailedDescription: `Education is the foundation for breaking the cycle of poverty. This campaign aims to build a fully-equipped primary school in a rural African community where children currently have no access to formal education.

The school will accommodate 500 students and include 10 classrooms, a library, computer lab, playground, and sanitation facilities. We will also hire and train local teachers, ensuring sustainable operation for years to come.

Construction will be completed in phases over 12 months. We're partnering with local builders and using locally-sourced materials wherever possible to support the community economy. The school design incorporates rainwater harvesting and solar panels for sustainability.

Your donation will directly contribute to giving these children the education they deserve and a chance at a brighter future.`,
    image: "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800&h=600&fit=crop",
    category: "Education",
    country: "Kenya",
    targetAmount: 250000,
    currentAmount: 145000,
    startDate: "2024-02-01",
    endDate: "2025-02-01",
    status: "active",
  },
  {
    id: "3",
    title: "Winter Clothing for Syrian Refugees",
    ngoName: "International Relief Fund",
    organizationId: "1",
    organizationName: "International Relief Fund",
    organizationLogo: "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=200&h=200&fit=crop",
    transparencyScore: 94,
    description: "Warm clothing and blankets for refugee families facing harsh winter conditions",
    detailedDescription: `As winter approaches, thousands of Syrian refugee families in camps are facing freezing temperatures without adequate protection. This campaign provides winter clothing packages and thermal blankets to help them survive the cold months ahead.

Each winter package includes:
- Thermal blankets
- Winter coats for adults and children
- Warm clothing (sweaters, thermal underwear)
- Winter boots
- Sleeping bags

We've identified 2,000 families in urgent need. Each package costs $75 and will keep a family warm throughout the winter. Our team is already on the ground, ready to distribute supplies as soon as we reach our funding goal.

No family should suffer through winter without warmth. Your generosity can make the difference between comfort and suffering.`,
    image: "https://images.unsplash.com/photo-1509099836639-18ba1795216d?w=800&h=600&fit=crop",
    category: "Humanitarian Aid",
    country: "Turkey",
    targetAmount: 150000,
    currentAmount: 89000,
    startDate: "2024-11-01",
    endDate: "2024-03-31",
    status: "active",
  },
  {
    id: "4",
    title: "Clean Water Wells in Bangladesh",
    ngoName: "Clean Water Initiative",
    organizationId: "3",
    organizationName: "Clean Water Initiative",
    organizationLogo: "https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=200&h=200&fit=crop",
    transparencyScore: 91,
    description: "Installing 20 water wells to provide clean drinking water to rural villages",
    detailedDescription: `Access to clean water is a fundamental human right, yet millions in rural Bangladesh still lack this basic necessity. This campaign will install 20 deep tube wells in villages where families currently walk miles daily to fetch water, often from contaminated sources.

Each well costs $3,000 to install and can serve up to 300 people for 20+ years. Our wells are equipped with hand pumps requiring no electricity, making them sustainable and maintenance-free for communities.

The impact goes beyond just clean water:
- Reduced waterborne diseases
- Children can attend school instead of fetching water
- Women have more time for education and income generation
- Improved community health and productivity

We work with local engineers and involve community members in the installation process, ensuring they can maintain the wells long-term. Every well is marked with a plaque recognizing donors' contributions.`,
    image: "https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=800&h=600&fit=crop",
    category: "Water & Sanitation",
    country: "Bangladesh",
    targetAmount: 60000,
    currentAmount: 34000,
    startDate: "2024-01-10",
    endDate: "2024-12-31",
    status: "active",
  },
  {
    id: "5",
    title: "Medical Supplies for Yemen Crisis",
    ngoName: "International Relief Fund",
    organizationId: "1",
    organizationName: "International Relief Fund",
    organizationLogo: "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=200&h=200&fit=crop",
    transparencyScore: 94,
    description: "Essential medical supplies and equipment for hospitals treating war victims",
    detailedDescription: `Yemen's healthcare system has collapsed after years of conflict, leaving hospitals without basic medical supplies. This campaign provides essential medicines, surgical equipment, and medical supplies to hospitals treating the most vulnerable patients.

Your donations will purchase:
- Antibiotics and pain medications
- Surgical supplies and equipment
- IV fluids and blood bags
- Bandages and wound care supplies
- Emergency trauma kits

We work directly with three hospitals serving the hardest-hit regions, ensuring supplies reach those who need them most. Our medical coordinator on the ground oversees all deliveries and provides photographic evidence of distribution.

Every $100 can provide critical care for 10 patients. In this humanitarian crisis, your donation can literally save lives.`,
    image: "https://images.unsplash.com/photo-1584982751601-97dcc096659c?w=800&h=600&fit=crop",
    category: "Healthcare",
    country: "Yemen",
    targetAmount: 200000,
    currentAmount: 158000,
    startDate: "2024-01-01",
    endDate: "2024-12-31",
    status: "active",
  },
  {
    id: "6",
    title: "Orphan Education Support Program",
    ngoName: "Hope for Children Foundation",
    organizationId: "2",
    organizationName: "Hope for Children Foundation",
    organizationLogo: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=200&h=200&fit=crop",
    transparencyScore: 87,
    description: "Providing school supplies, uniforms, and tutoring for 200 orphaned children",
    detailedDescription: `This program supports 200 orphaned children by ensuring they have everything needed to succeed in school. Many of these children live with extended family members who struggle financially and cannot afford educational expenses.

Each child receives:
- Complete school uniform
- Backpack and school supplies for the year
- Textbooks and notebooks
- After-school tutoring twice weekly
- Nutritious lunch on school days

The program runs for a full academic year and costs $200 per child. We partner with local schools and hire qualified tutors from the community, creating jobs while supporting education.

Education provides these children hope for a better future. Your donation ensures that losing their parents doesn't mean losing their chance at education and a successful life.

We provide quarterly progress reports for each child, including photos, report cards, and messages from the students thanking donors.`,
    image: "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800&h=600&fit=crop",
    category: "Education",
    country: "Pakistan",
    targetAmount: 40000,
    currentAmount: 28500,
    startDate: "2024-09-01",
    endDate: "2025-06-30",
    status: "active",
  },
];

export const featuredCampaignIds = ["1", "2", "3"];

export const categories = [
  "All Categories",
  "Food Aid",
  "Education",
  "Healthcare",
  "Water & Sanitation",
  "Humanitarian Aid",
  "Emergency Relief",
];

export const countries = [
  "All Countries",
  "Palestine",
  "Kenya",
  "Turkey",
  "Bangladesh",
  "Yemen",
  "Pakistan",
  "Syria",
];
