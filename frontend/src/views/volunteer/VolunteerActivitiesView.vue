<template>
  <div>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="activities"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
        data-cy="volunteerActivitiesTable"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
            <v-spacer />
          </v-card-title>
        </template>
        <template v-slot:[`item.themes`]="{ item }">
          <v-chip v-for="theme in item.themes" v-bind:key="theme.id">
            {{ theme.completeName }}
          </v-chip>
        </template>
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip v-if="item.state === 'APPROVED'" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="red"
                v-on="on"
                data-cy="reportButton"
                @click="reportActivity(item)"
                >warning</v-icon
              >
            </template>
            <span>Report Activity</span>
          </v-tooltip>
          <v-tooltip v-if="activityAppliable(item)" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="blue"
                v-on="on"
                data-cy="applyButton"
                @click="newEnrollment(item)"
                >fa-solid fa-arrow-right</v-icon
              >
            </template>
            <span>Apply to Activiy</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <enrollment-dialog
        v-if="currentEnrollment && currentActivity && enrollmentDialog"
        v-model="enrollmentDialog"
        :activity="currentActivity"
        :enrollment="currentEnrollment"
        v-on:apply="applyActivity"
        v-on:close-enrollment-dialog="onCloseEnrollmentDialog"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Activity from '@/models/activity/Activity';
import Enrollment from '@/models/enrollment/Enrollment';
import EnrollmentDialog from '@/views/volunteer/EnrollmentDialog.vue';
import { show } from 'cli-cursor';

@Component({
  components: {
   'enrollment-dialog': EnrollmentDialog,
  },
  methods: { show },
})
export default class VolunteerActivitiesView extends Vue {
  activities: Activity[] = [];
  enrollments: Enrollment[] = [];

  currentEnrollment: Enrollment | null = null;
  currentActivity: Activity | null = null;
  enrollmentDialog: boolean = false;

  search: string = '';
  headers: object = [
    {
      text: 'Name',
      value: 'name',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Region',
      value: 'region',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Participants',
      value: 'participantsNumberLimit',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Themes',
      value: 'themes',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Description',
      value: 'description',
      align: 'left',
      width: '30%',
    },
    {
      text: 'State',
      value: 'state',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Start Date',
      value: 'formattedStartingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'End Date',
      value: 'formattedEndingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Application Deadline',
      value: 'formattedApplicationDeadline',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.activities = await RemoteServices.getActivities();
      this.enrollments = await RemoteServices.getVolunteerEnrollments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async reportActivity(activity: Activity) {
    if (activity.id !== null) {
      try {
        const result = await RemoteServices.reportActivity(
          this.$store.getters.getUser.id,
          activity.id,
        );
        this.activities = this.activities.filter((a) => a.id !== activity.id);
        this.activities.unshift(result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  newEnrollment(activity: Activity) {
    this.currentEnrollment = new Enrollment();
    this.currentActivity = activity;
    this.enrollmentDialog = true;
    console.log(this.currentActivity && this.currentEnrollment && this.enrollmentDialog);
  }

  onCloseEnrollmentDialog() {
    this.currentEnrollment = null;
    this.currentActivity = null;
    this.enrollmentDialog = false;
  }

  async applyActivity(activity: Activity, enrollment: Enrollment) {
    if (activity.id !== null) {
      try {
        const result = await RemoteServices.createEnrollment(
          activity.id,
          enrollment,
        );
        this.enrollments.push(result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  activityAppliable(activity: Activity) {
    if (activity.id === null) {
      return false;
    }
    let applicationDeadline = new Date(activity.applicationDeadline);
    let today = new Date();
    today.setHours(0, 0, 0, 0);
    let commonEnrollments = this.enrollments.some(
      (enrollment) => enrollment.activityId === activity.id,
    );
    return applicationDeadline >= today && !commonEnrollments;
  }
}
</script>

<style lang="scss" scoped></style>
